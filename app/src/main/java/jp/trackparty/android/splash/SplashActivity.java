package jp.trackparty.android.splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import bolts.Continuation;
import bolts.Task;
import jp.trackparty.android.R;
import jp.trackparty.android.base.BaseFragmentActivity;
import jp.trackparty.android.data.shared_prefs.Authentication;
import jp.trackparty.android.login.LoginActivity;
import jp.trackparty.android.login.LoginViewModel;
import jp.trackparty.android.main.MainActivity;
import jp.trackparty.android.serverconfig.ServerConfigActivity;

public class SplashActivity extends BaseFragmentActivity implements AuthTokenVerificationViewModelObserver.Callback {

    private Authentication.StateObserver authStateObserver;
    private AuthTokenVerificationViewModelObserver authTokenVerificationViewModelObserver;

    public static Intent newIntent(Context context) {
        return new Intent(context, SplashActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_activity_layout);
        showFragment(SplashFragment.newInstance());

        authStateObserver = new Authentication.StateObserver(this);
        authStateObserver.setCallback(new Authentication.StateObserver.Callback() {
            @Override
            public void onStateUpdated(Authentication.State state) {
                switch (state) {
                    case SERVER_CONFIG_REQUIRED:
                        showServerConfigActivity();
                        finish();
                    case AUTHENTICATION_REQUIRED:
                        showLoginActivity();
                        finish();
                        break;
                }
            }
        });

        authTokenVerificationViewModelObserver = new AuthTokenVerificationViewModelObserver();
        authTokenVerificationViewModelObserver.setCallback(this);

        if (savedInstanceState == null) {
            AuthTokenVerificationViewModel.requestTokenVerification()
                    .onSuccess(new Continuation<Void, Object>() {
                        @Override
                        public Object then(Task<Void> task) throws Exception {
                            AuthTokenVerificationService.start(SplashActivity.this);
                            return null;
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        authStateObserver.subscribe();
        authTokenVerificationViewModelObserver.subscribe();
    }

    @Override
    protected void onPause() {
        authTokenVerificationViewModelObserver.unsubscribe();
        authStateObserver.unsubscribe();
        super.onPause();
    }

    private void showServerConfigActivity() {
        startActivity(ServerConfigActivity.newIntent(this));
    }

    private void showLoginActivity() {
        startActivity(LoginActivity.newIntent(this));
    }

    private void showMainActivity() {
        Intent intent = MainActivity.newIntent(this);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onUpdateAuthTokenVerificationViewModel(AuthTokenVerificationViewModel authTokenVerificationViewModel) {
        if (authTokenVerificationViewModel != null &&
                authTokenVerificationViewModel.tokenVerified &&
                authTokenVerificationViewModel.state == LoginViewModel.STATE_DONE) {
            showMainActivity();
            AuthTokenVerificationViewModel.delete();
            finish();
        }
    }
}
