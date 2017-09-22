package jp.trackparty.android.splash;

import android.support.annotation.Nullable;

import org.json.JSONObject;

import bolts.Task;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;

public class AuthTokenVerificationViewModel extends RealmObject {
    @PrimaryKey
    public int id = 0;

    public int state;
    public static int STATE_READY = 0;
    public static int STATE_IN_PROGRESS = 1;
    public static int STATE_DONE = 2;

    //request


    //response
    public boolean tokenVerified;
    public String lastError;

    public static @Nullable
    AuthTokenVerificationViewModel getCurrentSnapshot() {
        return RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<AuthTokenVerificationViewModel>() {
            @Override
            public AuthTokenVerificationViewModel execute(Realm realm) throws Exception {
                return realm.where(AuthTokenVerificationViewModel.class).equalTo("id", 0).findFirst();
            }
        });
    }

    public static Task<Void> requestTokenVerification() {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(AuthTokenVerificationViewModel.class, new JSONObject()
                        .put("id", 0)
                        .put("state", STATE_READY)
                        .put("tokenVerified", false));
            }
        });
    }

    public static Task<Void> delete() {
        return RealmHelper.getInstance().executeTransaction(new RealmHelper.Transaction() {
            @Override
            public void execute(Realm realm) throws Exception {
                realm.where(AuthTokenVerificationViewModel.class).equalTo("id", 0).findAll().deleteAllFromRealm();
            }
        });
    }
}
