package jp.trackparty.android.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import bolts.Continuation;
import bolts.Task;
import jp.trackparty.android.R;
import jp.trackparty.android.base.BaseFragmentActivity;
import jp.trackparty.android.data.shared_prefs.Authentication;
import jp.trackparty.android.etc.TextUtils;
import jp.trackparty.android.main.MainActivity;
import jp.trackparty.android.onesignal.OneSignalPlayerIdRegistrationService;
import jp.trackparty.android.serverconfig.ServerConfigActivity;

;

public class LoginActivity extends BaseFragmentActivity implements LoginViewModelObserver.Callback {
    private static final String KEY_ONE_TIME_PASSWORD = "one_time_password";
    private Authentication.StateObserver authStateObserver;
    private LoginViewModelObserver loginViewModelObserver;

    public static Intent newIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    public static Intent newIntent(Context context, String oneTimePassword) {
        Intent intent = newIntent(context);
        intent.putExtra(KEY_ONE_TIME_PASSWORD, oneTimePassword);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_activity_layout);

        asserServerConfigured();

        authStateObserver = new Authentication.StateObserver(this);
        authStateObserver.setCallback(new Authentication.StateObserver.Callback() {
            @Override
            public void onStateUpdated(Authentication.State state) {
                switch (state) {
                    case TOKEN_EXISTS:
                        showMainActivity();
                        LoginViewModel.delete();
                        finish();
                        OneSignalPlayerIdRegistrationService.start(LoginActivity.this);
                        break;
                }
            }
        });

        loginViewModelObserver = new LoginViewModelObserver();
        loginViewModelObserver.setCallback(this);

        handleIntentIfNeeded();
        restartLoginServiceIfNeeded();
    }


    @Override
    protected void onResume() {
        super.onResume();
        authStateObserver.subscribe();
        loginViewModelObserver.subscribe();
    }

    @Override
    protected void onPause() {
        loginViewModelObserver.unsubscribe();
        authStateObserver.unsubscribe();
        super.onPause();
    }


    private void showMainActivity() {
        Intent intent = MainActivity.newIntent(this);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void asserServerConfigured() {
        boolean isServerConfigured = Authentication.hasServer(this);
        if (!isServerConfigured) {
            showServerConfigActivity();
            finish();
        }
    }

    private void handleIntentIfNeeded() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(KEY_ONE_TIME_PASSWORD)) {
            LoginViewModel.setOneTimePasswordForLogin(intent.getStringExtra(KEY_ONE_TIME_PASSWORD))
                    .onSuccess(new Continuation<Void, Object>() {
                        @Override
                        public Object then(Task<Void> task) throws Exception {
                            LoginService.start(LoginActivity.this);
                            return null;
                        }
                    });
        }
    }

    private void restartLoginServiceIfNeeded() {
        LoginViewModel loginViewModel = LoginViewModel.getCurrentSnapshot();
        if (loginViewModel != null && loginViewModel.state != LoginViewModel.STATE_DONE) {
            // ここで止まっているときは、大抵の場合、LoginServiceが死んでるので、
            // 強制的にサービス再起動をかける
            LoginViewModel.updateStateToReady()
                    .onSuccess(new Continuation<Void, Object>() {
                        @Override
                        public Object then(Task<Void> task) throws Exception {
                            LoginService.start(LoginActivity.this);
                            return null;
                        }
                    });
        }
    }

    private void showServerConfigActivity() {
        startActivity(ServerConfigActivity.newIntent(this));
    }

    private void showInputEmailFragment() {
        showFragment(LoginInputEmailFragment.newInstance());
    }

    @Override
    public void onUpdateLoginViewModel(LoginViewModel loginViewModel) {
        if (loginViewModel == null) {
            showInputEmailFragment();
            return;
        }

        if (loginViewModel.state == LoginViewModel.STATE_IN_PROGRESS) {
            showFragment(LoginProgressFragment.newInstance());
            return;
        }

        if (!TextUtils.isEmpty(loginViewModel.authToken) && loginViewModel.state == LoginViewModel.STATE_DONE) {
            Authentication.putToken(this, loginViewModel.authToken);
            // MainActivityへの遷移はauthStateObserver側でおこなう
            return;
        }

        if (!TextUtils.isEmpty(loginViewModel.email) && loginViewModel.password == null &&
                loginViewModel.state == LoginViewModel.STATE_DONE && TextUtils.isEmpty(loginViewModel.lastError)) {
            showFragment(LoginEmailSentFragment.newInstance());
            return;
        }

        showInputEmailFragment();
    }
}
