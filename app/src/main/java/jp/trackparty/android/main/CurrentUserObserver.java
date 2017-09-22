package jp.trackparty.android.main;

import io.realm.Realm;
import io.realm.RealmQuery;
import jp.trackparty.android.data.realm.User;
import jp.trackparty.android.etc.realm.RealmObjectObserver;

class CurrentUserObserver extends RealmObjectObserver<User> {
    public interface Callback {
        void onUpdateUser(User user);
    }

    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected final RealmQuery<User> query(Realm realm) {
        return realm.where(User.class).equalTo("isCurrentUser", true);
    }

    @Override
    protected final void onUpdateRealmObject(User user) {
        if (callback != null) callback.onUpdateUser(user);
    }
}
