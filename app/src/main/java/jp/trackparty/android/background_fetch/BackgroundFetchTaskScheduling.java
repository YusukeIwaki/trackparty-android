package jp.trackparty.android.background_fetch;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;

public class BackgroundFetchTaskScheduling {
    private static BackgroundFetchTaskScheduling INSTANCE;;

    public static void initialize(Context context) {
        INSTANCE = new BackgroundFetchTaskScheduling(context);
    }
    public static BackgroundFetchTaskScheduling getInstance() {
        if (INSTANCE ==null) throw new IllegalStateException("not initialized!");

        return INSTANCE;
    }

    private GcmNetworkManager gcmNetworkManager;

    private BackgroundFetchTaskScheduling(Context context) {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            gcmNetworkManager = GcmNetworkManager.getInstance(context);
        }
    }

    public void enable() {
        if (gcmNetworkManager ==null) return;

        PeriodicTask task = new PeriodicTask.Builder()
                .setPeriod(BackgroundFetchService.BACKGROUND_FETCH_INTERVAL_SEC)
                .setPersisted(true) //再起動後にアプリを立ち上げて無くても実行
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED) //ネットワーク接続が必要
                .setService(BackgroundFetchService.class)
                .setTag(BackgroundFetchService.TAG_PERIODIC_FETCH)
                .setUpdateCurrent(true)
                .build();
        gcmNetworkManager.schedule(task);
    }

    public void disable() {
        if (gcmNetworkManager ==null) return;

        gcmNetworkManager.cancelTask(BackgroundFetchService.TAG_PERIODIC_FETCH, BackgroundFetchService.class);
    }
}