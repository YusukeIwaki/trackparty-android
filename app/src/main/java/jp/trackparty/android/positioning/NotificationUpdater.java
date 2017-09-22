package jp.trackparty.android.positioning;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;

import bolts.Continuation;
import bolts.Task;
import jp.trackparty.android.data.realm.LocationHistory;

/**
 * LocationHistoryの最新のログをもとに、
 * Notificationを更新します
 */
class NotificationUpdater {
    private final Context context;
    private final int notificationId;
    private final LastLocationHistoryObserver locationHistoryObserver;
    private final LastLocationHistoryObserver.Callback callback = new LastLocationHistoryObserver.Callback() {
        @Override
        public void onUpdateLastLocation(@NonNull LocationHistory locationHistory) {
            new NotificationBuilder(context).buildLocationNotification(locationHistory)
                    .onSuccess(new Continuation<Notification, Object>() {
                        @Override
                        public Object then(Task<Notification> task) throws Exception {
                            NotificationManagerCompat.from(context)
                                    .notify(notificationId, task.getResult());

                            return null;
                        }
                    });
        }
    };

    public NotificationUpdater(Context context, int notificationId) {
        this.context = context;
        this.notificationId = notificationId;

        locationHistoryObserver = new LastLocationHistoryObserver();
        locationHistoryObserver.setCallback(callback);
    }

    public void enable() {
        locationHistoryObserver.subscribe();
    }

    public void disable() {
        locationHistoryObserver.unsubscribe();
    }
}
