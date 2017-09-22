package jp.trackparty.api;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import bolts.Task;
import jp.trackparty.api.exception.ServerCofigurationRequiredException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*package*/ class ApiV1Impl extends ApiBase implements ApiV1 {
    private static final String AUTH_TOKEN_HEADER_KEY = "X-TrackParty-User-Token";
    private final String hostname;
    private final AuthenticationProvider authenticationProvider;
    private final BaseOkHttpClientProvider baseOkHttpClientProvider;

    // 何か送っておかないとPOST/PATCHはOkHttpでエラーになるので、それ用
    private static final JSONObject DUMMY_BODY = JsonCreator.createJson(new JsonCreator.CreateJsonObjectImpl() {
        @Override
        public JSONObject createJsonObject(JSONObject newJsonObject) throws JSONException {
            return newJsonObject.put("trackparty", "1");
        }
    });

    /**
     * @param baseOkHttpClientProvider OkHttpClientがnullの最初の１回だけ呼ばれます！（使う側でシングルトンな作りにする必要はありません！）
     */
    public ApiV1Impl(@NonNull String hostname,
                     AuthenticationProvider authenticationProvider,
                     BaseOkHttpClientProvider baseOkHttpClientProvider) {
        if (hostname == null) throw new ServerCofigurationRequiredException();
        this.hostname = hostname;
        this.authenticationProvider = authenticationProvider;
        this.baseOkHttpClientProvider = baseOkHttpClientProvider;
    }

    /**
     * 認証トークンを保持していたら、それをHTTPヘッダに付けます。
     */
    private class AuthTokenHeaderInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request req = chain.request();

            String token = authenticationProvider.getToken();
            if (token != null) {
                req = req.newBuilder()
                        .header(AUTH_TOKEN_HEADER_KEY, token)
                        .build();
            }

            return chain.proceed(req);
        }
    }

    /**
     * HTTP 401のレスポンスが来たらローカルに保持している認証トークンなどをすべて消します。
     */
    private class AuthRequiredInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            if (response.code() == 401) {
                // ログインAPI側でハンドリングするはずなので、それ以外の401のみ
                if ("/api/v1/session".equals(request.url().encodedPath()) && "POST".equals(request.method())) {
                    return response;
                }
                // ローカルに保持している認証トークンなどを全消しする
                authenticationProvider.clearToken();
            }
            return response;
        }
    }

    private OkHttpClient.Builder baseOkHttpClientBuilder() {
        if (baseOkHttpClientProvider != null) {
            return baseOkHttpClientProvider.getBaseOkHttpClient().newBuilder();
        } else {
            return new OkHttpClient.Builder();
        }
    }

    @Override
    protected OkHttpClient buildOkHttpClient() {
        return baseOkHttpClientBuilder()
                .addInterceptor(new AuthTokenHeaderInterceptor())
                .addInterceptor(new AuthRequiredInterceptor())
                .build();
    }

    private HttpUrl.Builder baseUrlBuilder() {
        return new HttpUrl.Builder()
                .scheme("https")
                .host(hostname);
    }

    @Override
    public Task<JSONObject> createOneTimePassword(final String email) {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/one_time_passwords")
                .build();

        JSONObject body = JsonCreator.createJson(new JsonCreator.CreateJsonObjectImpl() {
            @Override
            public JSONObject createJsonObject(JSONObject newJsonObject) throws JSONException {
                return newJsonObject
                        .put("email", email)
                        .put("sendmail", true);
            }
        });

        return baseJsonPOST(url, body);
    }

    @Override
    public Task<JSONObject> createSession(final String email, final String password) {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/session")
                .build();

        JSONObject body = JsonCreator.createJson(new JsonCreator.CreateJsonObjectImpl() {
            @Override
            public JSONObject createJsonObject(JSONObject newJsonObject) throws JSONException {
                return newJsonObject
                        .put("email", email)
                        .put("password", password);
            }
        });

        return baseJsonPOST(url, body);
    }

    @Override
    public Task<JSONObject> createSession(final String ontTimePassword) {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/session")
                .build();

        JSONObject body = JsonCreator.createJson(new JsonCreator.CreateJsonObjectImpl() {
            @Override
            public JSONObject createJsonObject(JSONObject newJsonObject) throws JSONException {
                return newJsonObject.put("one_time_password", ontTimePassword);
            }
        });

        return baseJsonPOST(url, body);
    }

    @Override
    public Task<JSONObject> updateSession() {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/session")
                .build();

        return baseJsonPATCH(url, DUMMY_BODY);
    }

    @Override
    public Task<JSONObject> deleteSession() {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/session")
                .build();

        return baseJsonDELETE(url);
    }

    @Override
    public Task<JSONObject> getCurrentUser() {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/user")
                .build();

        return baseJsonGET(url);
    }

    @Override
    public Task<JSONObject> getUserTransportPlan() {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/user/transport_plan")
                .build();

        return baseJsonGET(url);
    }

    @Override
    public Task<JSONObject> getDestinationDetail(long id) {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/destinations/" + id)
                .build();

        return baseJsonGET(url);
    }

    @Override
    public Task<JSONObject> uploadLocationLog(final String uuid, final double latitude, final double longitude, final double accuracy, final long timestamp) {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/user/location_logs")
                .build();

        JSONObject body = JsonCreator.createJson(new JsonCreator.CreateJsonObjectImpl() {
            @Override
            public JSONObject createJsonObject(JSONObject newJsonObject) throws JSONException {
                return newJsonObject
                        .put("id", uuid)
                        .put("latitude", latitude)
                        .put("longitude", longitude)
                        .put("accuracy", accuracy)
                        .put("timestamp", timestamp);
            }
        });

        return baseJsonPOST(url, body);
    }

    @Override
    public Task<JSONObject> registerPushNotificationEndpoint(final String identifier) {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/user/push_notification_endpoints")
                .build();

        JSONObject body = JsonCreator.createJson(new JsonCreator.CreateJsonObjectImpl() {
            @Override
            public JSONObject createJsonObject(JSONObject newJsonObject) throws JSONException {
                return newJsonObject
                        .put("identifier", identifier);
            }
        });

        return baseJsonPATCH(url, body);
    }

    @Override
    public Task<JSONObject> startTransportItem(long transportItemId) {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/transport_items/" + transportItemId + "/start")
                .build();

        return baseJsonPOST(url, DUMMY_BODY);
    }

    @Override
    public Task<JSONObject> pauseOngoingTransport() {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/user/ongoing_transport/pause")
                .build();

        return baseJsonPOST(url, DUMMY_BODY);
    }

    @Override
    public Task<JSONObject> resumeOngoingTransport() {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/user/ongoing_transport/resume")
                .build();

        return baseJsonPOST(url, DUMMY_BODY);
    }

    @Override
    public Task<JSONObject> completeOngoingTransport() {
        HttpUrl url = baseUrlBuilder()
                .addPathSegments("api/v1/user/ongoing_transport/complete")
                .build();

        return baseJsonPOST(url, DUMMY_BODY);
    }
}
