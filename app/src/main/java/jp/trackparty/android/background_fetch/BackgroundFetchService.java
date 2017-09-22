package jp.trackparty.android.background_fetch;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import jp.trackparty.android.data.realm.OngoingTransport;
import jp.trackparty.android.positioning.OneShotPositioningService;

public class BackgroundFetchService extends GcmTaskService {
    public static final String TAG_PERIODIC_FETCH = BackgroundFetchService.class.getName()+"$PERIODIC_FETCH";
    public static final long BACKGROUND_FETCH_INTERVAL_SEC = 300; //秒ごとに実行

    @Override
    public int onRunTask(TaskParams taskParams) {
        if (OngoingTransport.getCurrentTransportItem() == null) {
            startService(OneShotPositioningService.newIntent(this));
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
