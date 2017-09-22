package jp.trackparty.android.arrival;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import jp.trackparty.android.R;
import jp.trackparty.android.base.BaseActivity;

/**
 * 到着しました！のねぎらい画面
 */
public class ArrivalActivity extends BaseActivity {
    private static final String KEY_NEXT_INTENT = "nextIntent";
    private @Nullable Intent nextIntent;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            finish();
        }
    };
    private static final int MSG_FINISH = 0;
    private static final int FINISH_DELAY = 3500;

    public static Intent newIntent(Context context) {
        return new Intent(context, ArrivalActivity.class);
    }

    public static Intent newIntent(Context context, Intent nextIntent) {
        Intent intent = newIntent(context);
        intent.putExtra(KEY_NEXT_INTENT, nextIntent);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrival);

        nextIntent = parseNextIntent();

        if (savedInstanceState == null) {
            handler.sendEmptyMessageDelayed(MSG_FINISH, FINISH_DELAY);
        }
    }

    private Intent parseNextIntent() {
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(KEY_NEXT_INTENT)) return null;

        return intent.getParcelableExtra(KEY_NEXT_INTENT);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        handler.removeMessages(MSG_FINISH);
        if (nextIntent == null) super.finish();
        else {
            nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(nextIntent);
        }
    }
}
