package jp.trackparty.api;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import jp.trackparty.api.exception.HttpException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

abstract class ApiBase {
    private OkHttpClient okHttpClient = null;

    private OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = buildOkHttpClient();
        }
        return okHttpClient;
    }

    protected abstract OkHttpClient buildOkHttpClient();

    private Task<Response> baseRequest(Request request) {
        final TaskCompletionSource<Response> tcs = new TaskCompletionSource<>();

        getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                tcs.setError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    tcs.setResult(response);
                } else {
                    tcs.setError(new HttpException(response));
                }
            }
        });

        return tcs.getTask();
    }

    private Task<JSONObject> baseJsonRequest(Request request) {
        return baseRequest(request).onSuccess(new Continuation<Response, JSONObject>() {
            @Override
            public JSONObject then(Task<Response> task) throws Exception {
                // retrofit2/OkHttpCall.java を少しだけ参考にした
                Response response = task.getResult();

                ResponseBody responseBody = response.body();
                if (responseBody == null) return null;

                int code = response.code();

                if (code == 204 || code == 205) {
                    responseBody.close();
                    return null;
                }

                return new JSONObject(responseBody.string());
            }
        });
    }

    private static final MediaType mediaTypeApplicationJson = MediaType.parse("application/json; charset=utf-8");

    protected Task<JSONObject> baseJsonPOST(HttpUrl url, JSONObject requestBody) {
        RequestBody body = RequestBody.create(mediaTypeApplicationJson, requestBody.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return baseJsonRequest(request);
    }

    protected Task<JSONObject> baseJsonGET(HttpUrl url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return baseJsonRequest(request);
    }

    protected Task<JSONObject> baseJsonPATCH(HttpUrl url, JSONObject requestBody) {
        RequestBody body = RequestBody.create(mediaTypeApplicationJson, requestBody.toString());
        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .build();
        return baseJsonRequest(request);
    }

    protected Task<JSONObject> baseJsonDELETE(HttpUrl url) {
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        return baseJsonRequest(request);
    }

}
