package jp.trackparty.android.splash;

import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.realm.Realm;
import jp.trackparty.android.api.TrackpartyApi;
import jp.trackparty.android.base.BaseOneShotService;
import jp.trackparty.android.data.shared_prefs.Authentication;
import jp.trackparty.android.etc.TextUtils;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;

/**
 * LoginCredentialのtokenVerifiedがfalseのときに
 * PUT /api/v1/session を呼んで、トークンの正当性を確かめて、
 * tokenVerifiedをtrueにします。
 *
 * トークンがない時や、トークンが検証済みのときは何もしません。
 */
public class AuthTokenVerificationService extends BaseOneShotService {
    private static final String TAG = AuthTokenVerificationService.class.getSimpleName();

    public static Intent newIntent(Context context) {
        return new Intent(context, AuthTokenVerificationService.class);
    }

    public static void start(Context context) {
        context.startService(newIntent(context));
    }

    @Override
    protected Task<Void> doService() {
        if (TextUtils.isEmpty(Authentication.getToken(this, null))) return Task.forError(new IllegalStateException("no tokens exists."));

        final AuthTokenVerificationViewModel authTokenVerificationViewModel = RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<AuthTokenVerificationViewModel>() {
            @Override
            public AuthTokenVerificationViewModel execute(Realm realm) throws Exception {
                return realm.where(AuthTokenVerificationViewModel.class)
                        .equalTo("id", 0)
                        .equalTo("state", AuthTokenVerificationViewModel.STATE_READY)
                        .findFirst();
            }
        });

        if (authTokenVerificationViewModel == null) return Task.forResult(null);

        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(AuthTokenVerificationViewModel.class, new JSONObject()
                        .put("id", 0)
                        .put("state", AuthTokenVerificationViewModel.STATE_IN_PROGRESS));
            }
        }).onSuccessTask(new Continuation<Void, Task<JSONObject>>() {
            @Override
            public Task<JSONObject> then(Task<Void> task) throws Exception {
                return TrackpartyApi.newInstance(getBaseContext()).updateSession();
            }
        }).continueWithTask(new Continuation<JSONObject, Task<Void>>() {
            @Override
            public Task<Void> then(Task<JSONObject> task) throws Exception {
                final JSONObject json = task.isFaulted() ?
                        new JSONObject().put("tokenVerified", false).put("lastError", task.getError().getMessage()) :
                        new JSONObject().put("tokenVerified", true).put("lastError", JSONObject.NULL);

                json.put("id", 0).put("state", AuthTokenVerificationViewModel.STATE_DONE);

                return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
                    @Override
                    public void execute(Realm realm) throws Exception {
                        realm.createOrUpdateObjectFromJson(AuthTokenVerificationViewModel.class, json);
                    }
                });
            }
        });
    }
}
