package jp.trackparty.android.data.shared_prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import jp.trackparty.android.etc.ReactiveSharedPref;

public class Authentication {
    private static final String PREF_NAME = "authentication";
    public static final String KEY_SERVER = "server";
    public static final String KEY_TOKEN = "token";

    private static SharedPreferences get(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getServer(Context context, String defaultValue) {
        return get(context).getString(KEY_SERVER, defaultValue);
    }

    public static boolean hasServer(Context context) {
        return !TextUtils.isEmpty(get(context).getString(KEY_SERVER, null));
    }

    public static void setServer(Context context, String server) {
        get(context).edit()
                .putString(KEY_SERVER, server)
                .apply();
    }

    /**
     * トークンを取得。
     */
    public static String getToken(Context context, String defaultValue) {
        return get(context).getString(KEY_TOKEN, defaultValue);
    }

    /**
     * 認証APIからもらったトークンなど、確認済みのトークンを保存します
     */
    public static void putToken(Context context, String token) {
        get(context).edit()
                .putString(KEY_TOKEN, token)
                .apply();
    }

    /**
     * トークンを削除します
     */
    public static void delete(Context context) {
        get(context).edit()
                .remove(KEY_TOKEN)
                .apply();
    }

    public enum State {
        SERVER_CONFIG_REQUIRED,
        AUTHENTICATION_REQUIRED,
        TOKEN_EXISTS
    }

    public static class StateObserver extends ReactiveSharedPref<State> implements ReactiveSharedPref.OnUpdateListener<State> {
        public interface Callback {
            void onStateUpdated(State state);
        }
        private Callback callback;

        public StateObserver(Context context) {
            super(Authentication.get(context));

            setObservationPolicy(new ReactiveSharedPref.ObservationPolicy<State>() {
                @Override
                public boolean isTargetKey(String key) {
                    return true;
                }

                @Override
                public State getValueFromSharedPreference(SharedPreferences prefs) {
                    boolean hasServer = !TextUtils.isEmpty(prefs.getString(Authentication.KEY_SERVER, null));
                    boolean hasToken = !TextUtils.isEmpty(prefs.getString(Authentication.KEY_TOKEN, null));

                    if (!hasServer) {
                        return State.SERVER_CONFIG_REQUIRED;
                    } else if(!hasToken) {
                        return State.AUTHENTICATION_REQUIRED;
                    } else {
                        return State.TOKEN_EXISTS;
                    }
                }
            });

            setOnUpdateListener(this);
        }

        public void setCallback(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void onPreferenceValueUpdated(State state) {
            if (callback != null) callback.onStateUpdated(state);
        }
    }
}
