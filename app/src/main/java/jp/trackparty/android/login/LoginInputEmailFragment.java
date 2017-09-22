package jp.trackparty.android.login;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import bolts.Continuation;
import bolts.Task;
import jp.trackparty.android.R;
import jp.trackparty.android.etc.TextUtils;

;

public class LoginInputEmailFragment extends BaseLoginFragment {
    public static LoginInputEmailFragment newInstance() {
        return new LoginInputEmailFragment();
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_login_input_email;
    }

    @Override
    protected void onSetupView() {
        setInitialStateFromViewModel();

        rootView.findViewById(R.id.btn_create_one_time_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView txtEmail = (TextView) rootView.findViewById(R.id.editor_email);
                createOneTimePassword(txtEmail.getText().toString().trim());
            }
        });

        rootView.findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView txtEmail = (TextView) rootView.findViewById(R.id.editor_email);
                TextView txtPassword = (TextView) rootView.findViewById(R.id.editor_password);
                login(txtEmail.getText().toString().trim(), txtPassword.getText().toString().trim());
            }
        });

        showOrHidePasswordEditor(false);
        rootView.findViewById(R.id.btn_show_password_editor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHidePasswordEditor(true);
            }
        });
    }

    private void setInitialStateFromViewModel() {
        LoginViewModel loginViewModel = LoginViewModel.getCurrentSnapshot();
        if (loginViewModel == null) return;

        TextView txtError = (TextView) rootView.findViewById(R.id.txt_error);
        TextView txtEmail = (TextView) rootView.findViewById(R.id.editor_email);
        TextView txtPassword = (TextView) rootView.findViewById(R.id.editor_password);

        txtEmail.setText(TextUtils.emptyIfNull(loginViewModel.email));
        txtPassword.setText(TextUtils.emptyIfNull(loginViewModel.password));
        txtError.setText(TextUtils.emptyIfNull(loginViewModel.lastError));
    }

    private void createOneTimePassword(String email) {
        if (!TextUtils.isEmpty(email)) {
            final Context context = getContext();
            LoginViewModel.setEmailForCreateOneTimeToken(email)
                    .onSuccess(new Continuation<Void, Object>() {
                        @Override
                        public Object then(Task<Void> task) throws Exception {
                            LoginService.start(context);
                            return null;
                        }
                    });
        }
    }

    private void showOrHidePasswordEditor(boolean show) {
        View txtPassword = rootView.findViewById(R.id.editor_password_container);
        View btnCreateOneTimePassword = rootView.findViewById(R.id.btn_create_one_time_password);
        View txtOr = rootView.findViewById(R.id.txt_or);
        View btnShowPasswordEditor = rootView.findViewById(R.id.btn_show_password_editor);
        View btnLogin = rootView.findViewById(R.id.btn_login);

        if (show) {
            txtPassword.setVisibility(View.VISIBLE);
            btnCreateOneTimePassword.setVisibility(View.GONE);
            txtOr.setVisibility(View.GONE);
            btnShowPasswordEditor.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
        } else {
            txtPassword.setVisibility(View.GONE);
            btnCreateOneTimePassword.setVisibility(View.VISIBLE);
            txtOr.setVisibility(View.VISIBLE);
            btnShowPasswordEditor.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);
        }
    }

    private void login(String email, String password) {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            final Context context = getContext();
            LoginViewModel.setEmailAndPasswordForLogin(email, password)
                    .onSuccess(new Continuation<Void, Object>() {
                        @Override
                        public Object then(Task<Void> task) throws Exception {
                            LoginService.start(context);
                            return null;
                        }
                    });
        }
    }
}
