package jp.trackparty.android.transport_item_list;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;
import io.realm.Realm;
import jp.trackparty.android.api.TrackpartyApi;
import jp.trackparty.android.base.BaseOneShotService;
import jp.trackparty.api.ApiV1;

public class TransportItemListService extends BaseOneShotService {
    private ApiV1 apiV1;
    public static void start(Context context) {
        Intent intent = new Intent(context, TransportItemListService.class);
        context.stopService(intent); //動いてるやつは一旦とめる。
        context.startService(intent);
    }

    @Override
    protected Task<Void> doService() {
        final TransportItemListViewModel transportItemListViewModel = RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<TransportItemListViewModel>() {
            @Override
            public TransportItemListViewModel execute(Realm realm) throws Exception {
                return realm.where(TransportItemListViewModel.class)
                        .equalTo("id", 0)
                        .equalTo("state", TransportItemListViewModel.STATE_READY)
                        .findFirst();
            }
        });

        if (transportItemListViewModel == null) return Task.forResult(null);

        apiV1 = TrackpartyApi.newInstance(this);

        // 進行中状態にする
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(TransportItemListViewModel.class, new JSONObject()
                        .put("id", 0)
                        .put("state", TransportItemListViewModel.STATE_IN_PROGRESS));
            }
        }).onSuccessTask(new Continuation<Void, Task<JSONObject>>() {
            @Override
            public Task<JSONObject> then(Task<Void> task) throws Exception {
                return apiV1.getUserTransportPlan();
            }
        }).onSuccessTask(new Continuation<JSONObject, Task<Void>>() {
            @Override
            public Task<Void> then(Task<JSONObject> task) throws Exception {
                final JSONObject json = getTransportPlanFrom(task.getResult())
                        .put("id", 0)
                        .put("state", TransportItemListViewModel.STATE_DONE)
                        .put("lastError", JSONObject.NULL);
                return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
                    @Override
                    public void execute(Realm realm) throws Exception {
                        realm.createOrUpdateObjectFromJson(TransportItemListViewModel.class, json);
                    }
                });
            }
        }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    final JSONObject json = new JSONObject()
                            .put("id", 0)
                            .put("state", TransportItemListViewModel.STATE_DONE)
                            .put("transport_items", JSONObject.NULL)
                            .put("lastError", task.getError().getMessage());
                    return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
                        @Override
                        public void execute(Realm realm) throws Exception {
                            realm.createOrUpdateObjectFromJson(TransportItemListViewModel.class, json);
                        }
                    });
                }
                return task;
            }
        });
    }

    private JSONObject getTransportPlanFrom(@Nullable JSONObject transportPlanJson) throws JSONException {
        if (transportPlanJson == null || transportPlanJson.isNull("transport_plan")) return new JSONObject().put("transport_items", new JSONArray());

        return transportPlanJson.getJSONObject("transport_plan");
    }
}
