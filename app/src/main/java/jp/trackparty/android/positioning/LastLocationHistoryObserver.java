package jp.trackparty.android.positioning;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import jp.trackparty.android.data.realm.LocationHistory;
import jp.trackparty.android.etc.realm.RealmObjectObserver;

class LastLocationHistoryObserver extends RealmObjectObserver<LocationHistory> {

    public interface Callback {
        void onUpdateLastLocation(@NonNull LocationHistory locationHistory);
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
    protected RealmResults<LocationHistory> execQuery(RealmQuery<LocationHistory> query) {
        return query.findAllSorted("timestamp", Sort.DESCENDING);
    }

    @Override
    protected LocationHistory extractObjectFromResults(RealmResults<LocationHistory> results) {
        return results.first(null);
    }

    @Override
    protected void onUpdateRealmObject(@Nullable LocationHistory locationHistory) {
        if (callback != null && locationHistory != null) {
            callback.onUpdateLastLocation(locationHistory);
        }
    }
}
