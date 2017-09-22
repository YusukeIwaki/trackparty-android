package jp.trackparty.api;

public class ApiV1Factory {
    /**
     * @param baseOkHttpClientProvider OkHttpClientがnullの最初の１回だけ呼ばれます！（使う側でシングルトンな作りにする必要はありません！）
     */
    public static ApiV1 createApiV1(String hostname,
                                    AuthenticationProvider authenticationProvider,
                                    BaseOkHttpClientProvider baseOkHttpClientProvider) {
        return new ApiV1Impl(hostname, authenticationProvider, baseOkHttpClientProvider);
    }
}
