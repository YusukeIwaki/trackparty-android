package jp.trackparty.android.positioning;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import jp.trackparty.android.R;


public class PositioningRequirementCheckAndStartPositioningActivity extends AppCompatActivity {

    private static final int RC_PERMISSION = 10;
    private static final int RC_LOCATION_SETTING = 11;

    private static final String KEY_NEXT_INTENT = "next_intent";
    private Intent nextIntent;

    private boolean hasValidLocationSettingsCache;
    private GoogleApiClient googleApiClient;
    private final GoogleApiClient.ConnectionCallbacks googleApiClientCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            checkAndStartPositioning();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    private GoogleApiClient.OnConnectionFailedListener googleApiClientFailureListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            showToast(R.string.requirement_check_failure_googleplay);
            finish();
        }
    };

    public static Intent newIntent(Context context) {
        return new Intent(context, PositioningRequirementCheckAndStartPositioningActivity.class);
    }

    public static Intent newIntent(Context context, Intent positioningServiceIntent) {
        Intent intent = newIntent(context);
        intent.putExtra(KEY_NEXT_INTENT, positioningServiceIntent);
        return intent;
    }

    private void showToast(int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleNextIntent();
        hasValidLocationSettingsCache = false;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(googleApiClientCallback)
                .addOnConnectionFailedListener(googleApiClientFailureListener)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    private void handleNextIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            nextIntent = intent.getParcelableExtra(KEY_NEXT_INTENT);
            if (nextIntent != null) return;
        }

        // デフォルトは、ワンショット位置情報取得サービス
        nextIntent = OneShotPositioningService.newIntent(this);
    }

    private void checkAndStartPositioning() {
        if (!hasValidLocationSettings()) {
            return;
        }

        if (!hasLocationPermission()) {
            final String[] requiredPermissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

            ActivityCompat.requestPermissions(this, requiredPermissions, RC_PERMISSION);
            return;
        }

        finish();
        startService(nextIntent);
    }

    private boolean hasValidLocationSettings() {
        if (hasValidLocationSettingsCache) return true;

        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(PositioningUtil.buildLocationRequest())
                .build();

        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest)
                .setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                        Status status = locationSettingsResult.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                hasValidLocationSettingsCache = true;
                                checkAndStartPositioning();
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                hasValidLocationSettingsCache = false;
                                // Location settings are not satisfied, but this can be fixed
                                // by showing the user a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(PositioningRequirementCheckAndStartPositioningActivity.this, RC_LOCATION_SETTING);
                                } catch (IntentSender.SendIntentException e) {
                                    showToast(R.string.requirement_check_failure_location_setting);
                                    finish();
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                hasValidLocationSettingsCache = false;
                                // Location settings are not satisfied. However, we have no way
                                // to fix the settings so we won't show the dialog.
                                showToast(R.string.requirement_check_failure_location_setting);
                                finish();
                                break;
                        }
                    }
                });
        return false;
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_PERMISSION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                checkAndStartPositioning();
                return;
            }
        }

        showToast(R.string.requirement_check_failure_location_permission);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != RC_LOCATION_SETTING) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            checkAndStartPositioning();
            return;
        }

        showToast(R.string.requirement_check_failure_location_setting);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
