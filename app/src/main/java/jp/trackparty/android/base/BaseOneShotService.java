package jp.trackparty.android.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import bolts.Continuation;
import bolts.Task;
import jp.trackparty.android.BuildConfig;

/**
 * 多重起動ができない＆ワンショットなことをやるサービスのベースクラス。
 */
public abstract class BaseOneShotService extends Service {
    private static final String TAG = BaseOneShotService.class.getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();

        doService().continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                if (task.isFaulted() && BuildConfig.DEBUG) {
                    Exception err = task.getError();
                    Log.e(TAG, err.getMessage(), err);
                }

                stopSelf();
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    protected abstract Task<Void> doService();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
