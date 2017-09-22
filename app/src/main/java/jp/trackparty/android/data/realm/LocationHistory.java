package jp.trackparty.android.data.realm;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.util.UUID;

import bolts.Continuation;
import bolts.Task;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;
import jp.trackparty.android.BuildConfig;
import jp.trackparty.android.location_upload.LocationUploadItem;
import jp.trackparty.android.location_upload.LocationUploadService;

public class LocationHistory extends RealmObject {
    @PrimaryKey
    public long timestamp;
    public double latitude;
    public double longitude;
    public double accuracy;

    public static @Nullable LocationHistory getLastLocation() {
        return RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<LocationHistory>() {
            @Override
            public LocationHistory execute(Realm realm) throws Exception {
                return realm.where(LocationHistory.class).findAllSorted("timestamp", Sort.DESCENDING).first(null);
            }
        });
    }

    public static Task<Void> saveLocationAndScheduleUpload(final Context context,
                                                           final double latitude,
                                                           final double longitude,
                                                           final double accuracy,
                                                           final long timestampMs) {

        return saveLocation(latitude, longitude, accuracy, timestampMs)
                .onSuccessTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        context.startService(LocationUploadService.newIntent(context));
                        return task;
                    }
                });
    }

    private static Task<Void> saveLocation(final double latitude,
                                          final double longitude,
                                          final double accuracy,
                                          final long timestampMs) {
        if (BuildConfig.DEBUG) {
            Log.d("LocationHistory", "(lat,lon) = ("+latitude+", "+longitude+"), acc="+accuracy+", ts="+timestampMs);
        }
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(LocationUploadItem.class, new JSONObject()
                        .put("uuid", UUID.randomUUID().toString())
                        .put("numRetry", 0)
                        .put("syncState", LocationUploadItem.STATE_NOT_SYNCED)
                        .put("locationHistory", new JSONObject()
                            .put("latitude", latitude)
                            .put("longitude", longitude)
                            .put("accuracy", accuracy)
                            .put("timestamp", timestampMs)));
            }
        });
    }
}
