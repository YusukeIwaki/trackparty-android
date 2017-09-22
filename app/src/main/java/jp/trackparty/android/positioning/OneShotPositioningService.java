package jp.trackparty.android.positioning;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import jp.trackparty.android.base.BaseOneShotService;
import jp.trackparty.android.data.realm.LocationHistory;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

/**
 * 位置情報を測位して、LocationHistoryに保存する
 * ワンショットサービス
 */
public class OneShotPositioningService extends BaseOneShotService {
    private static final String TAG = OneShotPositioningService.class.getSimpleName();

    private Looper positioningThreadLooper;

    private CountDownLatch countDownLatch;
    private Looper serviceThreadLooper;
    private Handler serviceThreadHandler;

    public static Intent newIntent(Context context) {
        return new Intent(context, OneShotPositioningService.class);
    }

    @Override
    protected Task<Void> doService() {
        prepareThreadHandlers();
        return doPositioningOneShot().onSuccessTask(new Continuation<Location, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Location> task) throws Exception {
                Location location = task.getResult();
                return LocationHistory.saveLocationAndScheduleUpload(OneShotPositioningService.this,
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAccuracy(),
                        location.getTime());
            }
        }).onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                // 測位終了のGoogle APIを叩くまでは、このサービスを落としてはいけない。
                return waitForLocationClientQuit();
            }
        });
    }

    private void prepareThreadHandlers() {
        // GoogleのLocationAPIに指定するためのスレッドを用意する。
        HandlerThread positioningThread = new HandlerThread(TAG + "_Postioning");
        positioningThread.start();
        positioningThreadLooper = positioningThread.getLooper();

        // Locationクライアントを動かすスレッド。
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        serviceThreadLooper = handlerThread.getLooper();
        serviceThreadHandler = new Handler(serviceThreadLooper);
    }

    @Override
    public void onDestroy() {
        serviceThreadLooper.quit();
        positioningThreadLooper.quit();
        super.onDestroy();
    }

    private Task<Location> doPositioningOneShot() {
        final TaskCompletionSource<Location> tcs = new TaskCompletionSource<>();
        serviceThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                doPositioning(tcs);
            }
        });
        return tcs.getTask();
    }

    private Task<Void> waitForLocationClientQuit() {
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        serviceThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                tcs.setResult(null);
            }
        });
        return tcs.getTask();
    }

    private void doPositioning(TaskCompletionSource<Location> tcs) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        ConnectionResult connectionResult = googleApiClient.blockingConnect();
        if (!connectionResult.isSuccess()) {
            tcs.setError(new Exception(String.format("onConnectionFailed: [%d] %s", connectionResult.getErrorCode(), connectionResult.getErrorMessage())));
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tcs.setError(new Exception("ACCESS_FINE_LOCATION/ACCESS_COARSE_LOCATION permission not granted."));
            return;
        }

        countDownLatch = new CountDownLatch(1);
        FusedLocationApi.requestLocationUpdates(googleApiClient, PositioningUtil.buildLocationRequest(), buildLocationListener(tcs), positioningThreadLooper);
        try {
            if (countDownLatch.await(13, TimeUnit.SECONDS)) {
                //位置情報が得られた
            } else {
                //タイムアウト
                Log.i(TAG, "requestLocationUpdates: timeout. Fallback to getLastLocation");
                Location location = FusedLocationApi.getLastLocation(googleApiClient);
                if (location != null) {
                    tcs.setResult(location);
                } else {
                    tcs.setError(new Exception("no location available"));
                }
            }
        } catch (InterruptedException e) {
            tcs.setError(e);
        }
        FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
    }

    private LocationListener buildLocationListener(final TaskCompletionSource<Location> tcs) {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                tcs.setResult(location);
                countDownLatch.countDown();
            }
        };
        return locationListener;
    }

    private LocationListener locationListener;
}
