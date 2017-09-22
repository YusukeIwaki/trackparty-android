package jp.trackparty.android.etc;

import android.support.annotation.Nullable;

public class TextUtils {
    public static boolean isEmpty(@Nullable CharSequence str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public static CharSequence emptyIfNull(@Nullable CharSequence text) {
        return text == null ? "" : text;
    }
}
