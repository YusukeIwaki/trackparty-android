package jp.trackparty.android.api;

import android.content.Context;

import jp.trackparty.android.data.shared_prefs.Authentication;
import jp.trackparty.api.AuthenticationProvider;

class TrackpartyApiAuthenticationProvider implements AuthenticationProvider {
    private Context context;
    public TrackpartyApiAuthenticationProvider(Context context) {
        this.context = context;
    }

    @Override
    public String getToken() {
        return Authentication.getToken(context, null);
    }

    @Override
    public void clearToken() {
        Authentication.delete(context);
    }
}
