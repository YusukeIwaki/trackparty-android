package jp.trackparty.android.etc.realm;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 */
abstract class AbstractRealmResultsObserver<T extends RealmObject> {
    protected Realm realm;
    private RealmChangeListener<RealmResults<T>> listener;
    private RealmResults<T> results;

    protected abstract RealmResults<T> queryItems(Realm realm);

    protected abstract RealmChangeListener<RealmResults<T>> getListener();

    public void subscribe() {
        unsubscribe();

        realm = Realm.getDefaultInstance();
        listener = getListener();
        results = queryItems(realm);

        listener.onChange(results);
        results.addChangeListener(listener);
    }

    public void unsubscribe() {
        if (realm != null) {
            if (results != null) {
                if (results.isValid()) {
                    results.removeChangeListener(listener);
                }
                results = null;
            }
            realm.close();
            realm = null;
        }
    }
}
