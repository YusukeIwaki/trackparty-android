package jp.trackparty.android.positioning;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.mypopsy.maps.StaticMap;

import java.util.concurrent.TimeUnit;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import jp.trackparty.android.R;
import jp.trackparty.android.data.realm.LocationHistory;
import jp.trackparty.android.main.MainActivity;

class NotificationBuilder {
    private final Context context;

    private static final int RC_MAIN_ACTIVITY = 1;
    private static final int RC_POSITIONING_MANUALLY = 10;

    public NotificationBuilder(Context context) {
        this.context = context;
    }

    private PendingIntent buildMainActivityPendingIntent() {
        return PendingIntent.getActivity(context, RC_MAIN_ACTIVITY, MainActivity.newIntent(context), 0);
    }

    private PendingIntent buildImmediatePositioningPendingIntent() {
        return PendingIntent.getActivity(context, RC_POSITIONING_MANUALLY, PositioningRequirementCheckAndStartPositioningActivity.newIntent(context), 0);
    }

    private NotificationCompat.Builder buildBaseNotificationBuilder() {
        return new NotificationCompat.Builder(context, null)
                .setContentTitle("バックグラウンドで実行中")
                .setContentIntent(buildMainActivityPendingIntent())
                .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                .addAction(R.drawable.ic_stat_positioning_now, "今すぐ測位",
                        buildImmediatePositioningPendingIntent());
    }

    public Notification buildBaseNotification() {
        return buildBaseNotificationBuilder().build();
    }

    public Task<Notification> buildLocationNotification(LocationHistory locationHistory) {
        return getStaticGoogleMapImage(locationHistory)
                .onSuccess(new Continuation<Bitmap, Notification>() {
                    @Override
                    public Notification then(Task<Bitmap> task) throws Exception {
                        return new NotificationCompat.BigPictureStyle(buildBaseNotificationBuilder())
                                .bigPicture(task.getResult())
                                .build();
                    }
                });
    }

    private Task<Bitmap> getStaticGoogleMapImage(LocationHistory locationHistory) {
        final TaskCompletionSource<Bitmap> tcs = new TaskCompletionSource<>();

        final String url = new GoogleStaticMap(600, 256,
                locationHistory.latitude,
                locationHistory.longitude,
                locationHistory.accuracy)
                .buildUrl();

        new Thread() {
            @Override
            public void run() {
                try {
                    tcs.setResult(Glide.with(context)
                            .asBitmap()
                            .load(url)
                            .submit()
                            .get(4500, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    tcs.setError(e);
                }
            }
        }.start();
        return tcs.getTask();
    }

    private static class GoogleStaticMap {
        private final int width;
        private final int height;
        private final double lat;
        private final double lon;
        private final double accuracy;

        public GoogleStaticMap(int width, int height, double lat, double lon, double accuracy) {
            this.width = width;
            this.height = height;
            this.lat = lat;
            this.lon = lon;
            this.accuracy = accuracy;
        }

        private int calculateZoom() {
            int screenSize = Math.min(width, height);
            double requiredMpp =  10 * accuracy/screenSize;
            int zoomLevel = (int) (1 + Math.log(40075004 / (256 * requiredMpp)) / Math.log(2));

            if (zoomLevel < 1) return 1;
            if (zoomLevel > 20) return 20;
            return zoomLevel;
        }

        private StaticMap.Path circle(int step) {
            return StaticMap.Path.circle(StaticMap.Path.Style.builder().stroke(1).color(0xCC007AFF).fill(0x44007AFF).build(), lat, lon, (int) accuracy, 360/step);
        }

        public String buildUrl() {
            return new StaticMap()
                    .center(lat, lon)
                    .zoom(calculateZoom())
                    .size(width, height)
                    .marker(lat, lon)
                    .path(circle(6))
                    .toString();
        }
    }
}
