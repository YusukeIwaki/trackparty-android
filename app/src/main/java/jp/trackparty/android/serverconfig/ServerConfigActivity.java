package jp.trackparty.android.serverconfig;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import jp.trackparty.android.R;
import jp.trackparty.android.base.BaseActivity;
import jp.trackparty.android.data.shared_prefs.Authentication;
import jp.trackparty.android.login.LoginActivity;

public class ServerConfigActivity extends BaseActivity {
    public static Intent newIntent(Context context) {
        return new Intent(context, ServerConfigActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_config);

        findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView txtHostname = findViewById(R.id.editor_hostname);
                setHostname(txtHostname.getText().toString());
            }
        });

        EditText editorHostname = findViewById(R.id.editor_hostname);
        editorHostname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                showOrHideHostnamePostfix(!s.toString().contains("."));
            }
        });
    }

    private void showOrHideHostnamePostfix(boolean show) {
        findViewById(R.id.txt_hostname_postfix).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String appendHostnamePostfixIfNeeded(String hostname) {
        if (hostname.contains(".")) return hostname;
        return hostname + ".trackparty.jp";
    }

    private void setHostname(@Nullable String hostname) {
        if (!TextUtils.isEmpty(hostname)) {
            Authentication.setServer(this, appendHostnamePostfixIfNeeded(hostname));
            startActivity(LoginActivity.newIntent(this));
            finish();
        }
    }
}
