package jp.trackparty.android.splash;

import io.realm.Realm;
import io.realm.RealmQuery;
import jp.trackparty.android.etc.realm.RealmObjectObserver;

class AuthTokenVerificationViewModelObserver extends RealmObjectObserver<AuthTokenVerificationViewModel> {
    public interface Callback {
        void onUpdateAuthTokenVerificationViewModel(AuthTokenVerificationViewModel authTokenVerificationViewModel);
    }

    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected final RealmQuery<AuthTokenVerificationViewModel> query(Realm realm) {
        return realm.where(AuthTokenVerificationViewModel.class).equalTo("id", 0);
    }

    @Override
    protected final void onUpdateRealmObject(AuthTokenVerificationViewModel model) {
        if (callback != null) callback.onUpdateAuthTokenVerificationViewModel(model);
    }
}
