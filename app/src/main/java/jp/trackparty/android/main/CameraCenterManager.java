package jp.trackparty.android.main;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * Googleマップの、中心位置移動の制御をおこなう
 */
class CameraCenterManager {
    private LatLng center = null;
    private boolean shouldTrackCenter = true;

    public interface Impl {
        void moveToCenter(LatLng center);
    }
    public interface Callback {
        void onShouldTrackCenterStateChanged(boolean movedFromCenter);
    }

    private @NonNull
    final Impl impl;
    private @NonNull final Callback callback;

    public CameraCenterManager(Impl impl, Callback callback) {
        this.impl = impl;
        this.callback = callback;
    }

    public void onUserMove() {
        if (!shouldTrackCenter) return;

        shouldTrackCenter = false;
        callback.onShouldTrackCenterStateChanged(false);
    }

    public void updateCenterPosition(LatLng position) {
        center = position;
        if (shouldTrackCenter) {
            impl.moveToCenter(center);
        }
    }

    public void resetToCenter() {
        if (shouldTrackCenter) return;

        shouldTrackCenter = true;
        callback.onShouldTrackCenterStateChanged(true);
        if (center != null) {
            impl.moveToCenter(center);
        }
    }
}