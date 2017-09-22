package jp.trackparty.android.main;

import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import jp.trackparty.android.api.TrackpartyApi;
import jp.trackparty.android.base.BaseOneShotService;
import jp.trackparty.android.data.shared_prefs.Authentication;

public class LogoutService extends BaseOneShotService {
    private static final String TAG = LogoutService.class.getSimpleName();
    public static void start(Context context) {
        Intent intent = new Intent(context, LogoutService.class);
        context.startService(intent);
    }

    @Override
    protected Task<Void> doService() {
        return TrackpartyApi.newInstance(this).deleteSession()
                .continueWith(new Continuation<JSONObject, Void>() {
                    @Override
                    public Void then(Task<JSONObject> task) throws Exception {
                        //トークンが消えたことで、BaseAuthActivityのhandleAuthRequired()が呼ばれ、
                        //EntryPoint経由でログイン画面に遷移する
                        Authentication.delete(getBaseContext());

                        return null;
                    }
                });
    }
}
