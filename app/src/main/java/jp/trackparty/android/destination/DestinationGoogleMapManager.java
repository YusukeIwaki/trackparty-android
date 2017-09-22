package jp.trackparty.android.destination;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DestinationGoogleMapManager {
    private GoogleMap googleMap;
    private Marker mapMarker;

    private LatLng lastLatLng;

    public void setCenter(double lat, double lon) {
        lastLatLng = new LatLng(lat, lon);
        if (googleMap != null) {
            setCenterToLastLatLng();
        }
    }

    private void setCenterToLastLatLng() {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(lastLatLng));

        if (mapMarker == null) {
            mapMarker = googleMap.addMarker(new MarkerOptions()
                    .position(lastLatLng));
        } else {
            mapMarker.setPosition(lastLatLng);
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (lastLatLng != null) {
            setCenterToLastLatLng();
        }
    }
}
