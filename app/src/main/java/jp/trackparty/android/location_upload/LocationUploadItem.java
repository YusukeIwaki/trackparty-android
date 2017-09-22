package jp.trackparty.android.location_upload;

import bolts.Task;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.annotations.PrimaryKey;
import jp.trackparty.android.data.realm.LocationHistory;

public class LocationUploadItem extends RealmObject {
    @PrimaryKey public String uuid;
    public int syncState;
    public static final int STATE_NOT_SYNCED = 0;
    public static final int STATE_SYNCING = 1;
    public static final int STATE_SYNCED = 2;
    public static final int STATE_SYNC_ERROR = 3;

    public LocationHistory locationHistory;
    public int failureCount = 0;
    public String lastError;

    private static final int THRESHOLD_MS = 600_000; //10分以内のものだけアップロードする

    public static RealmQuery<LocationUploadItem> getTargetForRetry(Realm realm) {
        return realm.where(LocationUploadItem.class)
                .equalTo("syncState", STATE_SYNC_ERROR)
                .lessThan("failureCount", 10) //10回失敗したらさすがに諦める
                .isNotNull("locationHistory")
                .greaterThan("locationHistory.timestamp", System.currentTimeMillis() - THRESHOLD_MS);
    }

    public static RealmQuery<LocationUploadItem> getTargetForUpload(Realm realm) {
        return realm.where(LocationUploadItem.class)
                .equalTo("syncState", STATE_NOT_SYNCED)
                .isNotNull("locationHistory")
                .greaterThan("locationHistory.timestamp", System.currentTimeMillis() - THRESHOLD_MS);
    }

    public static Task<Void> scheduleRetry() {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                for (LocationUploadItem item : getTargetForRetry(realm).findAll()) {
                    item.syncState = STATE_NOT_SYNCED;
                }
            }
        });
    }
}
