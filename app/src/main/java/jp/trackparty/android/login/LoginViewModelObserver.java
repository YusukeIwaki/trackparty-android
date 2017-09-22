package jp.trackparty.android.login;

import io.realm.Realm;
import io.realm.RealmQuery;
import jp.trackparty.android.etc.realm.RealmObjectObserver;

class LoginViewModelObserver extends RealmObjectObserver<LoginViewModel> {
    public interface Callback {
        void onUpdateLoginViewModel(LoginViewModel loginViewModel);
    }

    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected final RealmQuery<LoginViewModel> query(Realm realm) {
        return realm.where(LoginViewModel.class).equalTo("id", 0);
    }

    @Override
    protected final void onUpdateRealmObject(LoginViewModel model) {
        if (callback != null) callback.onUpdateLoginViewModel(model);
    }
}
