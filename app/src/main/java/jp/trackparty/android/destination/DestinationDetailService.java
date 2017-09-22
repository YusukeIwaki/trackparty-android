package jp.trackparty.android.destination;

import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;
import io.realm.Realm;
import jp.trackparty.android.api.TrackpartyApi;
import jp.trackparty.android.base.BaseOneShotService;
import jp.trackparty.api.ApiV1;

public class DestinationDetailService extends BaseOneShotService {
    private ApiV1 apiV1;
    public static void start(Context context) {
        Intent intent = new Intent(context, DestinationDetailService.class);
        context.stopService(intent); //動いてるやつは一旦とめる。
        context.startService(intent);
    }

    @Override
    protected Task<Void> doService() {
        final DestinationDetailViewModel destinationDetailViewModel = RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<DestinationDetailViewModel>() {
            @Override
            public DestinationDetailViewModel execute(Realm realm) throws Exception {
                return realm.where(DestinationDetailViewModel.class)
                        .equalTo("state", DestinationDetailViewModel.STATE_READY)
                        .findFirst();
            }
        });

        if (destinationDetailViewModel == null) return Task.forResult(null);

        apiV1 = TrackpartyApi.newInstance(this);

        final long id = destinationDetailViewModel.id;
        // 進行中状態にする
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(DestinationDetailViewModel.class, new JSONObject()
                        .put("id", id)
                        .put("state", DestinationDetailViewModel.STATE_IN_PROGRESS));
            }
        }).onSuccessTask(new Continuation<Void, Task<JSONObject>>() {
            @Override
            public Task<JSONObject> then(Task<Void> task) throws Exception {
                return apiV1.getDestinationDetail(id);
            }
        }).onSuccessTask(new Continuation<JSONObject, Task<Void>>() {
            @Override
            public Task<Void> then(Task<JSONObject> task) throws Exception {
                final JSONObject json = task.getResult()
                        .put("id", id)
                        .put("state", DestinationDetailViewModel.STATE_DONE)
                        .put("lastError", JSONObject.NULL);
                return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
                    @Override
                    public void execute(Realm realm) throws Exception {
                        realm.createOrUpdateObjectFromJson(DestinationDetailViewModel.class, json);
                    }
                });
            }
        }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    final JSONObject json = new JSONObject()
                            .put("id", id)
                            .put("state", DestinationDetailViewModel.STATE_DONE)
                            .put("destination", JSONObject.NULL)
                            .put("lastError", task.getError().getMessage());
                    return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
                        @Override
                        public void execute(Realm realm) throws Exception {
                            realm.createOrUpdateObjectFromJson(DestinationDetailViewModel.class, json);
                        }
                    });
                }
                return task;
            }
        });
    }
}
