package jp.trackparty.android.main;

import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmQuery;
import jp.trackparty.android.data.realm.OngoingTransport;
import jp.trackparty.android.data.realm.TransportItem;
import jp.trackparty.android.etc.realm.RealmObjectObserver;

class OngoingTransportItemObserver extends RealmObjectObserver<OngoingTransport> {
    public interface Callback {
        void onUpdateOngoingTransportItem(@Nullable TransportItem transportItem);
    }

    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected final RealmQuery<OngoingTransport> query(Realm realm) {
        return realm.where(OngoingTransport.class)
                .equalTo("id", 0)
                .isNotNull("transportItem");
    }

    @Override
    protected final void onUpdateRealmObject(@Nullable OngoingTransport model) {
        if (callback != null) {
            if (model != null) callback.onUpdateOngoingTransportItem(model.transportItem);
            else callback.onUpdateOngoingTransportItem(null);
        }
    }
}
