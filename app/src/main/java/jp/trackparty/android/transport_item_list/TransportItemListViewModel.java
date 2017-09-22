package jp.trackparty.android.transport_item_list;

import org.json.JSONObject;

import bolts.Task;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import jp.trackparty.android.data.realm.TransportItem;

public class TransportItemListViewModel extends RealmObject {
    @PrimaryKey public int id = 0;

    public int state;
    public static int STATE_READY = 0;
    public static int STATE_IN_PROGRESS = 1;
    public static int STATE_DONE = 2;

    //request

    //response
    public RealmList<TransportItem> transport_items;
    public String lastError;


    public boolean isLoading() {
        return state != STATE_DONE;
    }

    public static Task<Void> forceScheduleUpdate() {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(TransportItemListViewModel.class, new JSONObject()
                        .put("id", 0)
                        .put("state", STATE_READY));
            }
        });
    }
}
