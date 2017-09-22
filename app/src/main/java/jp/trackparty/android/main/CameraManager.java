package jp.trackparty.android.main;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;

/**
 * GoogleMapで、ユーザーによるドラッグ操作をトラッキングする
 */
class CameraManager{
    public interface Callback {
        void onUserActivity();
    }

    private final @NonNull
    Callback callback;

    public CameraManager(@NonNull Callback callback) {
        this.callback = callback;
    }

    private static int NO_REASON = -1;
    private int lastStartReason = NO_REASON;

    public void onMoveStart(int reason) {
        lastStartReason = reason;
    }

    public void onMoveEnd() {
        if (lastStartReason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            callback.onUserActivity();
        }
    }

    public void onMoveCancel() {
        lastStartReason = NO_REASON;
    }
}
