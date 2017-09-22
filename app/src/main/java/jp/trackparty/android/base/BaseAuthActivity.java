package jp.trackparty.android.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import jp.trackparty.android.data.shared_prefs.Authentication;
import jp.trackparty.android.entry.EntrypointActivity;

/**
 * 認証トークンが無くなったらEntryPointに飛ぶActivity
 */
public abstract class BaseAuthActivity extends BaseActivity {
    private Authentication.StateObserver authStateObserver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authStateObserver = new Authentication.StateObserver(this);
        authStateObserver.setCallback(new Authentication.StateObserver.Callback() {
            @Override
            public void onStateUpdated(Authentication.State state) {
                if (state == Authentication.State.SERVER_CONFIG_REQUIRED ||
                        state == Authentication.State.AUTHENTICATION_REQUIRED) {
                    handleAuthRequired();
                }
            }
        });
    }

    /**
     * ４０１を受けたり、ログアウトしたり、などトークンが無い状態になったらここに入ってくる。
     */
    private void handleAuthRequired() {
        //TODO: ここでRealmのデータをいろいろ消す

        Intent intent = EntrypointActivity.newIntent(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        authStateObserver.subscribe();
    }

    @Override
    protected void onPause() {
        authStateObserver.unsubscribe();
        super.onPause();
    }
}
