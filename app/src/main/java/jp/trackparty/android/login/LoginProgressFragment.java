package jp.trackparty.android.login;

import jp.trackparty.android.R;

public class LoginProgressFragment extends BaseLoginFragment {
    public static LoginProgressFragment newInstance() {
        return new LoginProgressFragment();
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_login_progress;
    }

    @Override
    protected void onSetupView() {

    }
}
