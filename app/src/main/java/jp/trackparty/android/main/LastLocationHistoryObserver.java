package jp.trackparty.android.main;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmQuery;
import jp.trackparty.android.data.realm.LocationHistory;
import jp.trackparty.android.etc.realm.RealmObjectObserver;

public class LastLocationHistoryObserver extends RealmObjectObserver<LocationHistory> {
    public interface Callback {
        void onUpdateLocationHistory(@NonNull LocationHistory locationHistory);
    }

    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected RealmQuery<LocationHistory> query(Realm realm) {
        return realm.where(LocationHistory.class);
    }

    @Override
    protected final void onUpdateRealmObject(@Nullable LocationHistory locationHistory) {
        if (locationHistory != null) {
            callback.onUpdateLocationHistory(locationHistory);
        }
    }
}
