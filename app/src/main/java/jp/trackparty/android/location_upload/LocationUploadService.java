package jp.trackparty.android.location_upload;

import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.Sort;
import jp.trackparty.android.api.TrackpartyApi;
import jp.trackparty.android.base.BaseOneShotService;
import jp.trackparty.android.data.realm.LocationHistory;
import jp.trackparty.api.ApiV1;

public class LocationUploadService extends BaseOneShotService {
    private ApiV1 apiV1;

    public static Intent newIntent(Context context) {
        return new Intent(context, LocationUploadService.class);
    }

    @Override
    protected Task<Void> doService() {
        apiV1 = TrackpartyApi.newInstance(this);
        return LocationUploadItem.scheduleRetry()
                .onSuccessTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        return uploadLocationLogRecursive();
                    }
                });
    }

    /**
     * アップロード対称のLocationUploadItem(にひもづくLocationHistory)を、ひとつずつ順にアップロードする
     */
    private Task<Void> uploadLocationLogRecursive() {
        final List<LocationUploadItem> locationUploadItemList = RealmHelper.getInstance().executeTransactionForReadList(new RealmHelper.TransactionForRead<OrderedRealmCollection<LocationUploadItem>>() {
            @Override
            public OrderedRealmCollection<LocationUploadItem> execute(Realm realm) throws Exception {
                return LocationUploadItem.getTargetForUpload(realm)
                        .findAllSorted("locationHistory.timestamp", Sort.DESCENDING);
            }
        });

        // やることがない場合にはすぐにstopSelfする
        if (locationUploadItemList.isEmpty()) return Task.forResult(null);

        final LocationUploadItem lastLocationUploadItem = locationUploadItemList.get(0);

        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                // 進行中状態にする
                realm.createOrUpdateObjectFromJson(LocationUploadItem.class, new JSONObject()
                        .put("uuid", lastLocationUploadItem.uuid)
                        .put("syncState", LocationUploadItem.STATE_SYNCING));
            }
        }).onSuccessTask(new Continuation<Void, Task<JSONObject>>() {
            @Override
            public Task<JSONObject> then(Task<Void> task) throws Exception {
                LocationHistory h = lastLocationUploadItem.locationHistory;
                return apiV1.uploadLocationLog(lastLocationUploadItem.uuid, h.latitude, h.longitude, h.accuracy, h.timestamp);
            }
        }).onSuccessTask(new Continuation<JSONObject, Task<Void>>() {
            @Override
            public Task<Void> then(Task<JSONObject> task) throws Exception {
                // 完了状態にする
                final JSONObject json = new JSONObject()
                        .put("uuid", lastLocationUploadItem.uuid)
                        .put("syncState", LocationUploadItem.STATE_SYNCED)
                        .put("lastError", JSONObject.NULL);

                return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
                    @Override
                    public void execute(Realm realm) throws Exception {
                        realm.createOrUpdateObjectFromJson(LocationUploadItem.class, json);
                    }
                });
            }
        }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    // 失敗状態にする
                    final JSONObject json = new JSONObject()
                            .put("uuid", lastLocationUploadItem.uuid)
                            .put("syncState", LocationUploadItem.STATE_SYNC_ERROR)
                            .put("lastError", task.getError().getMessage())
                            .put("failureCount", lastLocationUploadItem.failureCount + 1);

                    return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
                        @Override
                        public void execute(Realm realm) throws Exception {
                            realm.createOrUpdateObjectFromJson(LocationUploadItem.class, json);
                        }
                    });
                }
                return task;
            }
        }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                // DBに変更を反映し終わって、あらためてアップロードできるものがあればやる
                return uploadLocationLogRecursive();
            }
        });
    }
}
