package jp.trackparty.android.onesignal;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.onesignal.OneSignal;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import jp.trackparty.android.api.TrackpartyApi;
import jp.trackparty.android.base.BaseOneShotService;
import jp.trackparty.android.data.shared_prefs.Authentication;
import jp.trackparty.android.etc.TextUtils;

/**
 * OneSignalのPlayerIDをAPI経由でRails側に登録するサービス
 */
public class OneSignalPlayerIdRegistrationService extends BaseOneShotService {
    private static final String TAG = OneSignalPlayerIdRegistrationService.class.getSimpleName();
    private static Intent newIntent(Context context) {
        return new Intent(context, OneSignalPlayerIdRegistrationService.class);
    }

    public static void start(Context context) {
        context.startService(newIntent(context));
    }

    @Override
    protected Task<Void> doService() {
        if (!Authentication.hasServer(this)) {
            Log.i(TAG, "hostname is not configured yet");
            return Task.forResult(null);
        }

        if (TextUtils.isEmpty(Authentication.getToken(this, null))) {
            Log.i(TAG, "Not authorized. Skip registration.");
            return Task.forResult(null);
        }

        return getPlayerIdAsync().onSuccessTask(new Continuation<String, Task<Void>>() {
            @Override
            public Task<Void> then(Task<String> task) throws Exception {
                final String playerId = task.getResult();
                return handleOneSignalPlayerId(playerId);
            }
        });
    }

    private Task<String> getPlayerIdAsync() {
        final TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                tcs.setResult(userId);
            }
        });
        return tcs.getTask();
    }

    private Task<Void> handleOneSignalPlayerId(String playerId) {
        return TrackpartyApi.newInstance(this).registerPushNotificationEndpoint(playerId).makeVoid();
    }

}
