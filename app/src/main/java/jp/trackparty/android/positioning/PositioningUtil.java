package jp.trackparty.android.positioning;

import com.google.android.gms.location.LocationRequest;

class PositioningUtil {
    public static LocationRequest buildLocationRequest() {
        return new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(4000)
                .setInterval(12000)
                ;
    }
}
