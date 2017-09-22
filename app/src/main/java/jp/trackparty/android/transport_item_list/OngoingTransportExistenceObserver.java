package jp.trackparty.android.transport_item_list;

import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmQuery;
import jp.trackparty.android.data.realm.OngoingTransport;
import jp.trackparty.android.etc.realm.RealmObjectObserver;

class OngoingTransportExistenceObserver extends RealmObjectObserver<OngoingTransport> {
    public interface Callback {
        void onUpdateOngoingTransportExistence(boolean exists);
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
        if (callback != null) callback.onUpdateOngoingTransportExistence(model != null);
    }
}
