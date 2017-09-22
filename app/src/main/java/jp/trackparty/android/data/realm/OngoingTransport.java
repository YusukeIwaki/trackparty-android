package jp.trackparty.android.data.realm;

import org.json.JSONObject;

import bolts.Task;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 選択中の目的地。
 */
public class OngoingTransport extends RealmObject {
    @PrimaryKey public int id;
    public TransportItem transportItem;

    public static Task<Void> setCurrentTransportItem(final long transportItemId) {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(OngoingTransport.class, new JSONObject()
                        .put("id", 0)
                        .put("transportItem", new JSONObject()
                                .put("id", transportItemId)));
            }
        });
    }

    public static Task<Void> resetCurrentTransportItem() {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.where(OngoingTransport.class).equalTo("id", 0).findAll().deleteAllFromRealm();
            }
        });
    }

    public static TransportItem getCurrentTransportItem() {
        return RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<TransportItem>() {
            @Override
            public TransportItem execute(Realm realm) throws Exception {
                OngoingTransport ongoingTransport = realm.where(OngoingTransport.class).equalTo("id", 0).findFirst();
                if (ongoingTransport == null) return null;
                return ongoingTransport.transportItem;
            }
        });
    }
}
