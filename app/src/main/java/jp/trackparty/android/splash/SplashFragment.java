package jp.trackparty.android.splash;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import bolts.Continuation;
import bolts.Task;
import jp.trackparty.android.R;
import jp.trackparty.android.base.BaseFragment;
import jp.trackparty.android.etc.TextUtils;

public class SplashFragment extends BaseFragment implements AuthTokenVerificationViewModelObserver.Callback {
    public static SplashFragment newInstance() {
        return new SplashFragment();
    }

    private View progressBar;
    private TextView errorText;
    private View btnRetry;

    private AuthTokenVerificationViewModelObserver authTokenVerificationViewModelObserver;

    @Override
    protected int getLayout() {
        return R.layout.fragment_splash;
    }

    @Override
    protected void onSetupView() {
        progressBar = rootView.findViewById(R.id.progress_bar);
        errorText = (TextView) rootView.findViewById(R.id.txt_error);
        btnRetry = rootView.findViewById(R.id.btn_retry);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = getContext();
                AuthTokenVerificationViewModel.requestTokenVerification()
                        .onSuccess(new Continuation<Void, Object>() {
                            @Override
                            public Object then(Task<Void> task) throws Exception {
                                AuthTokenVerificationService.start(context);
                                return null;
                            }
                        });
            }
        });

        authTokenVerificationViewModelObserver = new AuthTokenVerificationViewModelObserver();
        authTokenVerificationViewModelObserver.setCallback(this);
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
    }

    private void showOrHideErrorContainer(String error) {
        errorText.setText(error);

        progressBar.setVisibility(View.GONE);
        errorText.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);

    }

    @Override
    public void onUpdateAuthTokenVerificationViewModel(AuthTokenVerificationViewModel authTokenVerificationViewModel) {
        if (authTokenVerificationViewModel == null || authTokenVerificationViewModel.state != AuthTokenVerificationViewModel.STATE_DONE) {
            showProgress();
            return;
        }

        if (!TextUtils.isEmpty(authTokenVerificationViewModel.lastError)) {
            showOrHideErrorContainer(authTokenVerificationViewModel.lastError);
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        authTokenVerificationViewModelObserver.subscribe();
    }

    @Override
    public void onPause() {
        authTokenVerificationViewModelObserver.unsubscribe();
        super.onPause();
    }
}
