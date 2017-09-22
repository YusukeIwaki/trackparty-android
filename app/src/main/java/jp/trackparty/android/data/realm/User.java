package jp.trackparty.android.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {
    @PrimaryKey public long id;

    /**
     * ユーザの表示名
     */
    public String display_name;

    /**
     * ユーザのEmailアドレス。
     */
    public String email;

    /**
     * current_userかどうか
     */
    public boolean isCurrentUser;
}
