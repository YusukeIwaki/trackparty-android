package jp.trackparty.android.onesignal;

import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONException;
import org.json.JSONObject;

import jp.trackparty.android.positioning.OneShotPositioningService;

public class OneSignalNotificationExtenderService extends NotificationExtenderService {
    private static final String LOG_TAG = "TrackParty";

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult notification) {
        // trueを返却すると通知が捨てられる
        return shouldInterruptNotification(notification);
    }

    private boolean shouldInterruptNotification(OSNotificationReceivedResult notification) {
        JSONObject additionalData = notification.payload.additionalData;
        if (additionalData != null && !additionalData.isNull("send_to_sync")) {
            Log.i(LOG_TAG, "send_to_sync received!");

            try {
                handleSendToSync(additionalData);
            } catch (JSONException e) {
                Log.w(LOG_TAG, e.getMessage(), e);
            }

            return true;
        }
        return false;
    }

    private void handleSendToSync(JSONObject additionalData) throws JSONException {
        String syncType = additionalData.getString("sync_type");
        Log.i(LOG_TAG, "send_to_sync > type=" + syncType);

        // 「今すぐ測位」要求
        if ("positioning_request".equals(syncType)) {
            startService(OneShotPositioningService.newIntent(this));
            return;
        }

        // ...
        if ("...".equals(syncType)) {
            // ...
            return;
        }


    }
}
