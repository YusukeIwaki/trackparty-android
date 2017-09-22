package jp.trackparty.android.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 輸送アイテム
 */
public class TransportItem extends RealmObject {
    @PrimaryKey public long id;
    public Destination destination;
}
