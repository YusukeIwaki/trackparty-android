package jp.trackparty.android.etc.realm;

import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public abstract class RealmObjectObserver<T extends RealmObject> extends AbstractRealmResultsObserver<T> {
    private static final String TAG = RealmObjectObserver.class.getSimpleName();

    @Override
    protected final RealmResults<T> queryItems(Realm realm) {
        return execQuery(query(realm));
    }

    @Override
    protected final RealmChangeListener<RealmResults<T>> getListener() {
        return new RealmChangeListener<RealmResults<T>>() {
            private String previousResultString;

            @Override
            public void onChange(RealmResults<T> results) {
                T currentResult = extractObjectFromResults(results);
                String currentResultString = currentResult != null ? getComparationStringFor(currentResult) : "";
                if (previousResultString != null && previousResultString.equals(currentResultString)) {
                    return;
                }
                previousResultString = currentResultString;
                RealmObjectObserver.this.onUpdateRealmObject(currentResult != null ? realm.copyFromRealm(currentResult) : null);
            }
        };
    }

    protected abstract RealmQuery<T> query(Realm realm);
    protected abstract void onUpdateRealmObject(@Nullable T model);

    protected RealmResults<T> execQuery(RealmQuery<T> query) {
        return query.findAll();
    }

    protected T extractObjectFromResults(RealmResults<T> results) {
        return results.last(null);
    }

    protected String getComparationStringFor(T object) {
        return object.toString();
    }
}