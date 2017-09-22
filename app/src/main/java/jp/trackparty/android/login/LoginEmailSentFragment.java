package jp.trackparty.android.login;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import bolts.Continuation;
import bolts.Task;
import jp.trackparty.android.R;

public class LoginEmailSentFragment extends BaseLoginFragment {
    public static LoginEmailSentFragment newInstance() {
        return new LoginEmailSentFragment();
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_login_email_sent;
    }

    @Override
    protected void onSetupView() {
        rootView.findViewById(R.id.btn_login_with_one_time_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView txtOneTimePassword = (TextView) rootView.findViewById(R.id.editor_one_time_password);
                loginWithOneTimePassword(txtOneTimePassword.getText().toString());
            }
        });

        rootView.findViewById(R.id.btn_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = getContext();
                new AlertDialog.Builder(context)
                        .setTitle("メールを再送します")
                        .setPositiveButton("再送", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LoginViewModel.updateStateToReady()
                                        .onSuccess(new Continuation<Void, Object>() {
                                            @Override
                                            public Object then(Task<Void> task) throws Exception {
                                                LoginService.start(context);
                                                return null;
                                            }
                                        });
                            }
                        })
                        .setNeutralButton("入力し直す", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LoginViewModel.cancelOneTimePasswordLogin();
                            }
                        })
                        .show();
            }
        });
    }

    private void loginWithOneTimePassword(String oneTimePassword) {
        final Context context = getContext();
        LoginViewModel.setOneTimePasswordForLogin(oneTimePassword)
                .onSuccess(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        LoginService.start(context);
                        return null;
                    }
                });
    }
}
