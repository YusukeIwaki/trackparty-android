package jp.trackparty.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

class JsonCreator {
    private static final String TAG = JsonCreator.class.getSimpleName();

    public interface CreateJsonObjectImpl {
        JSONObject createJsonObject(JSONObject newJsonObject) throws JSONException;
    }

    public interface ErrorHandler {
        void onJsonException(JSONException e);
    }

    private final static ErrorHandler errorLogger = new ErrorHandler() {
        @Override
        public void onJsonException(JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    };

    public static JSONObject createJson(@NonNull CreateJsonObjectImpl impl) {
        return createJson(impl, errorLogger);
    }

    public static JSONObject createJson(@NonNull CreateJsonObjectImpl impl, @Nullable ErrorHandler errorHandler) {
        final JSONObject jsonObject = new JSONObject();
        try {
            impl.createJsonObject(jsonObject);
        } catch (JSONException e) {
            if (errorHandler != null) {
                errorHandler.onJsonException(e);
            }
        }
        return jsonObject;
    }
}
