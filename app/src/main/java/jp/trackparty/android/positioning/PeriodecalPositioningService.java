package jp.trackparty.android.positioning;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import jp.trackparty.android.data.realm.LocationHistory;

public class PeriodecalPositioningService extends Service {

    private static final String TAG = PeriodecalPositioningService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1284398; //適当な数字
    private static final String KEY_START = "start";

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Looper positioningThreadLooper;
    private NotificationUpdater notificationUpdater;

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            if (location != null) {
                LocationHistory.saveLocationAndScheduleUpload(PeriodecalPositioningService.this,
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAccuracy(),
                        location.getTime());
            }
        }
    };

    public static Intent newIntentForStarting(Context context) {
        return newIntent(context, true);
    }

    public static Intent newIntentForStopping(Context context) {
        return newIntent(context, false);
    }


    private static Intent newIntent(Context context, boolean start) {
        Intent intent = new Intent(context, PeriodecalPositioningService.class);
        intent.putExtra(KEY_START, start);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        positioningThreadLooper = createPositioningThreadLooper();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent.getBooleanExtra(KEY_START, true)) {
            onStart();
        } else {
            onStop();
        }
        return START_STICKY;
    }

    private static Looper createPositioningThreadLooper() {
        HandlerThread handlerThread = new HandlerThread(TAG+"_positioning");
        handlerThread.start();
        return handlerThread.getLooper();
    }

    @Override
    public void onDestroy() {
        if (notificationUpdater != null) {
            notificationUpdater.disable();
            notificationUpdater = null;
        }
        positioningThreadLooper.quit();
        super.onDestroy();
    }

    private void onStart() {
        try {
            requestLocationUpdate().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    startNotificationUpdate();
                }
            });
        } catch (SecurityException e) {
            Log.w(TAG, "Lost location permission. Could not request updates. " + e);
            stopForegroundServiceAndQuit();
        }
    }

    private void startNotificationUpdate() {
        startForeground(NOTIFICATION_ID, new NotificationBuilder(this).buildBaseNotification());
        if (notificationUpdater == null) {
            notificationUpdater = new NotificationUpdater(this, NOTIFICATION_ID);
        }
        notificationUpdater.enable();
    }

    private void onStop() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        stopForegroundServiceAndQuit();
    }

    private void stopForegroundServiceAndQuit() {
        stopForeground(true);
        stopSelf();
    }

    private Task<Void> requestLocationUpdate() throws SecurityException {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        return fusedLocationProviderClient.requestLocationUpdates(PositioningUtil.buildLocationRequest(), locationCallback, positioningThreadLooper);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
