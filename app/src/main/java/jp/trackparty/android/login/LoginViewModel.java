package jp.trackparty.android.login;

import android.support.annotation.Nullable;

import org.json.JSONObject;

import bolts.Task;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import jp.trackparty.android.etc.TextUtils;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;

public class LoginViewModel extends RealmObject {
    @PrimaryKey
    public int id = 0;

    public int state;
    public static int STATE_READY = 0;
    public static int STATE_IN_PROGRESS = 1;
    public static int STATE_DONE = 2;

    //request
    public String email;
    public String password;
    public String oneTimePassword;

    //response
    public String authToken;
    public String lastError;

    public static @Nullable LoginViewModel getCurrentSnapshot() {
        return RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<LoginViewModel>() {
            @Override
            public LoginViewModel execute(Realm realm) throws Exception {
                return realm.where(LoginViewModel.class).equalTo("id", 0).findFirst();
            }
        });
    }

    public static Task<Void> setOneTimePasswordForLogin(final String oneTimePassword) {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(LoginViewModel.class, new JSONObject()
                        .put("id", 0)
                        .put("email", JSONObject.NULL)
                        .put("password", JSONObject.NULL)
                        .put("oneTimePassword", oneTimePassword)
                        .put("state", STATE_READY));
            }
        });
    }

    public static Task<Void> setEmailForCreateOneTimeToken(final String email) {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(LoginViewModel.class, new JSONObject()
                        .put("id", 0)
                        .put("email", email)
                        .put("password", JSONObject.NULL)
                        .put("oneTimePassword", JSONObject.NULL)
                        .put("state", STATE_READY));
            }
        });
    }

    public static Task<Void> setEmailAndPasswordForLogin(final String email, final String password) {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(LoginViewModel.class, new JSONObject()
                        .put("id", 0)
                        .put("email", email)
                        .put("password", TextUtils.emptyIfNull(password))
                        .put("oneTimePassword", JSONObject.NULL)
                        .put("state", STATE_READY));
            }
        });
    }

    public static Task<Void> updateStateToReady() {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                LoginViewModel loginViewModel = realm.where(LoginViewModel.class).equalTo("id", 0).findFirst();
                if (loginViewModel != null) {
                    loginViewModel.state = STATE_READY;
                }
            }
        });
    }

    public static Task<Void> cancelOneTimePasswordLogin() {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(LoginViewModel.class, new JSONObject()
                        .put("id", 0)
                        .put("state", STATE_DONE)
                        .put("authToken", JSONObject.NULL)
                        .put("lastError", "キャンセルされました"));
            }
        });
    }

    public static Task<Void> delete() {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.where(LoginViewModel.class).equalTo("id", 0).findAll().deleteAllFromRealm();
            }
        });
    }
}
