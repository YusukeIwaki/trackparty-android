package jp.trackparty.android.login;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.realm.Realm;
import jp.trackparty.android.api.TrackpartyApi;
import jp.trackparty.android.base.BaseOneShotService;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;
import jp.trackparty.api.ApiV1;

public class LoginService extends BaseOneShotService {
    private ApiV1 apiV1;
    public static void start(Context context) {
        Intent intent = new Intent(context, LoginService.class);
        context.stopService(intent); //動いてるやつは一旦とめる。
        context.startService(intent);
    }

    @Override
    protected Task<Void> doService() {
        final LoginViewModel loginViewModel = RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<LoginViewModel>() {
            @Override
            public LoginViewModel execute(Realm realm) throws Exception {
                return realm.where(LoginViewModel.class)
                        .equalTo("id", 0)
                        .equalTo("state", LoginViewModel.STATE_READY)
                        .findFirst();
            }
        });

        if (loginViewModel == null) return Task.forResult(null);

        apiV1 = TrackpartyApi.newInstance(this);

        // 進行中状態にする
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(LoginViewModel.class, new JSONObject()
                        .put("id", 0)
                        .put("state", LoginViewModel.STATE_IN_PROGRESS));
            }
        }).onSuccessTask(new Continuation<Void, Task<JSONObject>>() {
            @Override
            public Task<JSONObject> then(Task<Void> task) throws Exception {
                return callAPIFor(loginViewModel);
            }
        }).onSuccessTask(new Continuation<JSONObject, Task<Void>>() {
            @Override
            public Task<Void> then(Task<JSONObject> task) throws Exception {
                final JSONObject json = task.getResult()
                        .put("id", 0)
                        .put("state", LoginViewModel.STATE_DONE)
                        .put("lastError", JSONObject.NULL);

                return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
                    @Override
                    public void execute(Realm realm) throws Exception {
                        realm.createOrUpdateObjectFromJson(LoginViewModel.class, json);
                    }
                });
            }
        }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    final JSONObject json = new JSONObject()
                            .put("id", 0)
                            .put("state", LoginViewModel.STATE_DONE)
                            .put("authToken", JSONObject.NULL)
                            .put("lastError", task.getError().getMessage());

                    return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
                        @Override
                        public void execute(Realm realm) throws Exception {
                            realm.createOrUpdateObjectFromJson(LoginViewModel.class, json);
                        }
                    });
                }
                return task;
            }
        });
    }

    private Task<JSONObject> callAPIFor(@NonNull LoginViewModel loginViewModel) {
        if (!TextUtils.isEmpty(loginViewModel.oneTimePassword)) {
            return apiV1.createSession(loginViewModel.oneTimePassword).onSuccess(new Continuation<JSONObject, JSONObject>() {
                @Override
                public JSONObject then(Task<JSONObject> task) throws Exception {
                    JSONObject result = task.getResult();
                    String token = result.getString("token");
                    return new JSONObject().put("authToken", token);
                }
            });
        }

        if (!TextUtils.isEmpty(loginViewModel.email)) {
            if (loginViewModel.password == null) {
                return apiV1.createOneTimePassword(loginViewModel.email);
            } else {
                return apiV1.createSession(loginViewModel.email, loginViewModel.password).onSuccess(new Continuation<JSONObject, JSONObject>() {
                    @Override
                    public JSONObject then(Task<JSONObject> task) throws Exception {
                        JSONObject result = task.getResult();
                        String token = result.getString("token");
                        return new JSONObject().put("authToken", token);
                    }
                });
            }
        }

        return Task.forError(new IllegalStateException("unknwon state"));
    }
}
