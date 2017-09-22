package jp.trackparty.android.data.realm;

import io.realm.RealmObject;

/**
 * 目的地
 */
public class Destination extends RealmObject {
    public long id;
    public String name;
    public String address;
    public double latitude;
    public double longitude;
}
