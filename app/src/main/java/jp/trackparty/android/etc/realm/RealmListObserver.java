package jp.trackparty.android.etc.realm;

import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

public abstract class RealmListObserver<T extends RealmObject> extends AbstractRealmResultsObserver<T> {
    private static final String TAG = RealmListObserver.class.getSimpleName();

    @Override
    protected RealmChangeListener<RealmResults<T>> getListener() {
        return new RealmChangeListener<RealmResults<T>>() {
            private String previousResultsString;

            @Override
            public void onChange(RealmResults<T> results) {
                String currentResultString = results != null ? getComparationStringFor(results) : "";
                if (previousResultsString != null && previousResultsString.equals(currentResultString)) {
                    return;
                }
                previousResultsString = currentResultString;
                onUpdateRealmList(realm.copyFromRealm(results));
            }
        };
    }

    protected abstract void onUpdateRealmList(List<T> models);

    protected String getComparationStringFor(RealmResults<T> results) {
        return results.toString();
    }
}