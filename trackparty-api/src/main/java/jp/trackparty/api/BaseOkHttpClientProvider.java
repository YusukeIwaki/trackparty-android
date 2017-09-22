package jp.trackparty.api;

import okhttp3.OkHttpClient;

public interface BaseOkHttpClientProvider {
    OkHttpClient getBaseOkHttpClient();
}
