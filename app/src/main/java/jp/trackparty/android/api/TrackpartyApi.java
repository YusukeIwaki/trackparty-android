package jp.trackparty.android.api;

import android.content.Context;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import jp.trackparty.android.data.shared_prefs.Authentication;
import jp.trackparty.api.ApiV1;
import jp.trackparty.api.ApiV1Factory;
import jp.trackparty.api.AuthenticationProvider;
import jp.trackparty.api.BaseOkHttpClientProvider;
import okhttp3.OkHttpClient;

public class TrackpartyApi {
    public static ApiV1 newInstance(Context context) {
        return ApiV1Factory.createApiV1(getHostname(context), getAuthenticationProvider(context), getBaseOkttpClientProvider());
    }

    private static String getHostname(Context context) {
        return Authentication.getServer(context, null);
    }

    private static AuthenticationProvider getAuthenticationProvider(Context context) {
        return new TrackpartyApiAuthenticationProvider(context);
    }

    private static BaseOkHttpClientProvider getBaseOkttpClientProvider() {
        return new BaseOkHttpClientProvider() {
            @Override
            public OkHttpClient getBaseOkHttpClient() {
                return new OkHttpClient.Builder()
                        .addNetworkInterceptor(new StethoInterceptor())
                        .build();
            }
        };
    }
}
