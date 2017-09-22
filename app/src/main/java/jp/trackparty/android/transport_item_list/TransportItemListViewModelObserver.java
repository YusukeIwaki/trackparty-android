package jp.trackparty.android.transport_item_list;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmQuery;
import jp.trackparty.android.etc.realm.RealmObjectObserver;

/**
 */
class TransportItemListViewModelObserver extends RealmObjectObserver<TransportItemListViewModel> {
    public interface Callback {
        void onUpdateTransportItemListViewModel(@NonNull TransportItemListViewModel viewModel);
    }
    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected final RealmQuery<TransportItemListViewModel> query(Realm realm) {
        return realm.where(TransportItemListViewModel.class).equalTo("id", 0);
    }

    @Override
    protected final void onUpdateRealmObject(@Nullable TransportItemListViewModel model) {
        if (callback != null && model != null) callback.onUpdateTransportItemListViewModel(model);
    }
}
