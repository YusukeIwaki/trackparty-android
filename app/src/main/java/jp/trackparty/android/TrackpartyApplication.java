package jp.trackparty.android;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.onesignal.OneSignal;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import jp.trackparty.android.background_fetch.BackgroundFetchTaskScheduling;
import jp.trackparty.android.onesignal.OneSignalPlayerIdRegistrationService;

public class TrackpartyApplication extends Application {
    private static final String TAG = TrackpartyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build());

        Stetho.initializeWithDefaults(this);

        BackgroundFetchTaskScheduling.initialize(this);

        OneSignal.startInit(this)
                .autoPromptLocation(false)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignalPlayerIdRegistrationService.start(this);
    }
}
