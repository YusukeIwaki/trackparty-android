package jp.trackparty.android.destination;

import org.json.JSONObject;

import bolts.Task;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import jp.trackparty.android.data.realm.Destination;

public class DestinationDetailViewModel extends RealmObject {
    @PrimaryKey public int id; //destination.idと同じ。

    public int state;
    public static int STATE_READY = 0;
    public static int STATE_IN_PROGRESS = 1;
    public static int STATE_DONE = 2;

    //request

    //response
    public Destination destination;
    public String lastError;


    public static Task<Void> forceScheduleUpdate(final long destinationId) {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(DestinationDetailViewModel.class, new JSONObject()
                        .put("id", destinationId)
                        .put("state", STATE_READY));
            }
        });
    }
}
