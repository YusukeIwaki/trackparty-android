package jp.trackparty.android.destination;

import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmQuery;
import jp.trackparty.android.data.realm.Destination;
import jp.trackparty.android.etc.realm.RealmObjectObserver;

class DestinationObserver extends RealmObjectObserver<Destination> {

    public interface Callback {
        void onUpdateDestination(@Nullable Destination destination);
    }
    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private final long id;

    public DestinationObserver(long id) {
        this.id = id;
    }

    @Override
    protected RealmQuery<Destination> query(Realm realm) {
        return realm.where(Destination.class).equalTo("id", id);
    }

    @Override
    protected void onUpdateRealmObject(@Nullable Destination model) {
        if (callback != null) callback.onUpdateDestination(model);
    }
}
