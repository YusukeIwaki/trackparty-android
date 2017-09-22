package jp.trackparty.android.entry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import jp.trackparty.android.data.shared_prefs.Authentication;
import jp.trackparty.android.etc.TextUtils;
import jp.trackparty.android.login.LoginActivity;
import jp.trackparty.android.serverconfig.ServerConfigActivity;
import jp.trackparty.android.splash.SplashActivity;

;

public class EntrypointActivity extends Activity {
    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, EntrypointActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        proceed();
        finish();
    }

    /**
     * 即座に判断できる分岐はここでする。
     * 非同期処理が必要なものは、SplashActivityに遷移させてその中でやる。
     */
    private void proceed() {
        Intent intent = getIntent();
        if (intent != null && handleIntent(intent)) {
            return;
        }

        if (TextUtils.isEmpty(Authentication.getServer(this, null))) {
            startActivity(ServerConfigActivity.newIntent(this));
            return;
        }

        if (TextUtils.isEmpty(Authentication.getToken(this, null))) {
            startActivity(LoginActivity.newIntent(this));
            return;
        }

        startActivity(SplashActivity.newIntent(this));
    }

    private boolean handleIntent(@NonNull Intent intent) {
        if (intent.getComponent() != null) {
            String className = intent.getComponent().getClassName();
            if (!TextUtils.isEmpty(className) && handleAliasIntent(intent, className)) return true;
        }

        return false;
    }

    private boolean handleAliasIntent(@NonNull Intent intent, @NonNull String className) {
        if (className.contains("OneTimePasswordReceiverActivity")) {
            Uri uri = intent.getData();
            if (uri != null) {
                String host = uri.getAuthority();
                if (!TextUtils.isEmpty(host) && host.equals(Authentication.getServer(this, null))) {
                    String oneTimePassword = uri.getLastPathSegment();
                    startActivity(LoginActivity.newIntent(this, oneTimePassword));
                    return true;
                }
            }
        }
        return false;
    }
}
