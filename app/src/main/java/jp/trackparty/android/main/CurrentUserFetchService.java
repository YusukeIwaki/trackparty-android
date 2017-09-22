package jp.trackparty.android.main;

import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.realm.Realm;
import jp.trackparty.android.api.TrackpartyApi;
import jp.trackparty.android.base.BaseOneShotService;
import jp.trackparty.android.data.realm.User;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;

/**
 * GET /api/v1/user を叩いて、current_user情報をUserに格納します。
 */
public class CurrentUserFetchService extends BaseOneShotService {
    public static Intent newIntent(Context context) {
        return new Intent(context, CurrentUserFetchService.class);
    }

    public static void start(Context context) {
        context.startService(newIntent(context));
    }

    @Override
    protected Task<Void> doService() {
        return TrackpartyApi.newInstance(this).getCurrentUser()
                .onSuccessTask(new Continuation<JSONObject, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<JSONObject> task) throws Exception {
                        return saveCurrentUser(task.getResult().getJSONObject("user"));
                    }
                });
    }

    private Task<Void> saveCurrentUser(final JSONObject currentUserJson) throws JSONException {
        currentUserJson.put("isCurrentUser", true);
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(User.class, currentUserJson);
            }
        });
    }
}