package jp.trackparty.android.main;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import jp.trackparty.android.data.realm.Destination;
import jp.trackparty.android.data.realm.LocationHistory;
import jp.trackparty.android.data.realm.TransportItem;

class GoogleMapManager {
    private GoogleMap googleMap;
    private Circle mapCircle;
    private Marker mapMarker;
    private Marker destMarker;
    private Polyline polyline;

    private LocationHistory lastLocationHistory;
    private Destination currentDestination;

    private LastLocationHistoryObserver lastLocationHistoryObserver;
    private LastLocationHistoryObserver.Callback locationHistoryCallback = new LastLocationHistoryObserver.Callback() {
        @Override
        public void onUpdateLocationHistory(@NonNull LocationHistory locationHistory) {
            lastLocationHistory = locationHistory;
            updateGoogleMap();
        }
    };

    private OngoingTransportItemObserver ongoingTransportItemObserver;
    private OngoingTransportItemObserver.Callback ongoingTransportItemCallback = new OngoingTransportItemObserver.Callback() {
        @Override
        public void onUpdateOngoingTransportItem(@Nullable TransportItem transportItem) {
            if (transportItem == null) {
                currentDestination = null;
            } else {
                currentDestination = transportItem.destination;
            }
            updateGoogleMap();
        }
    };

    private final CameraManager cameraManager;
    private final CameraManager.Callback cameraCallback = new CameraManager.Callback() {
        @Override
        public void onUserActivity() {
            if (googleMap != null && lastLocationHistory != null) {
                LatLng center = googleMap.getCameraPosition().target;
                double f = 0.0000001;
                boolean isCenter = (center.latitude < lastLocationHistory.latitude + f && center.latitude + f > lastLocationHistory.latitude &&
                        center.longitude < lastLocationHistory.longitude + f && center.longitude + f > lastLocationHistory.longitude);

                if (!isCenter) {
                    cameraCenterManager.onUserMove();
                }
            }
        }
    };

    private final CameraCenterManager cameraCenterManager;
    private final CameraCenterManager.Callback cameraCenterManagerCallback = new CameraCenterManager.Callback() {
        @Override
        public void onShouldTrackCenterStateChanged(boolean movedFromCenter) {
            if (autoTrackCenterStateCallback != null) autoTrackCenterStateCallback.onUpdateAutoTrackCenterState(movedFromCenter);
        }
    };
    private final CameraCenterManager.Impl cameraCenterManagerImpl = new CameraCenterManager.Impl() {
        @Override
        public void moveToCenter(LatLng center) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(center));
        }
    };

    public interface AutoTrackCenterStateCallback {
        /**
         * ユーザーによる操作があると、自動追跡しなくなります。
         * moveToCenterPositionが呼ばれると、ふたたび自動追跡するようになります。
         * その自動追跡状態を通知するためのインターフェースです。
         *
         * @param autoTrackCenter
         */
        void onUpdateAutoTrackCenterState(boolean autoTrackCenter);
    }

    private AutoTrackCenterStateCallback autoTrackCenterStateCallback;

    public void setAutoTrackCenterStateCallback(AutoTrackCenterStateCallback autoTrackCenterStateCallback) {
        this.autoTrackCenterStateCallback = autoTrackCenterStateCallback;
    }

    public GoogleMapManager() {
        lastLocationHistoryObserver = new LastLocationHistoryObserver();
        lastLocationHistoryObserver.setCallback(locationHistoryCallback);

        ongoingTransportItemObserver = new OngoingTransportItemObserver();
        ongoingTransportItemObserver.setCallback(ongoingTransportItemCallback);

        cameraManager = new CameraManager(cameraCallback);
        cameraCenterManager = new CameraCenterManager(cameraCenterManagerImpl, cameraCenterManagerCallback);
    }

    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (lastLocationHistory == null) {
            lastLocationHistory = LocationHistory.getLastLocation();
        }
        updateGoogleMap();

        autoTrackCenterStateCallback.onUpdateAutoTrackCenterState(true);
        googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                cameraManager.onMoveStart(reason);
            }
        });
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                cameraManager.onMoveEnd();
            }
        });
        googleMap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
            @Override
            public void onCameraMoveCanceled() {
                cameraManager.onMoveCancel();
            }
        });
    }

    public void enable() {
        lastLocationHistoryObserver.subscribe();
        ongoingTransportItemObserver.subscribe();
    }

    public void disable() {
        ongoingTransportItemObserver.unsubscribe();
        lastLocationHistoryObserver.unsubscribe();
    }

    private void updateGoogleMap() {
        if (googleMap != null && lastLocationHistory != null) {
            LatLng position = new LatLng(lastLocationHistory.latitude, lastLocationHistory.longitude);
            cameraCenterManager.updateCenterPosition(position);

            if (mapCircle == null) {
                mapCircle = googleMap.addCircle(new CircleOptions()
                        .center(position)
                        .radius(lastLocationHistory.accuracy)
                        .fillColor(Color.argb(16, 0, 64, 255))
                        .strokeColor(Color.argb(56, 0, 64, 255))
                        .strokeWidth(2));
            } else {
                mapCircle.setCenter(position);
                mapCircle.setRadius(lastLocationHistory.accuracy);
            }

            if (mapMarker == null) {
                mapMarker = googleMap.addMarker(new MarkerOptions()
                        .position(position));
            } else {
                mapMarker.setPosition(position);
            }

            if (currentDestination != null) {
                LatLng destPosition = new LatLng(currentDestination.latitude, currentDestination.longitude);
                if (polyline != null) {
                    polyline.remove();
                }
                polyline = googleMap.addPolyline(new PolylineOptions()
                        .width(5)
                        .color(Color.argb(87, 0, 64, 255))
                        .add(position, destPosition));

                if (destMarker == null) {
                    destMarker = googleMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .position(destPosition));
                } else {
                    destMarker.setPosition(destPosition);
                }
            } else {
                if (polyline != null) {
                    polyline.remove();
                    polyline = null;
                }

                if (destMarker != null) {
                    destMarker.remove();
                    destMarker = null;
                }
            }
        }
    }

    public void moveToCenterPosition() {
        cameraCenterManager.resetToCenter();
    }
}
