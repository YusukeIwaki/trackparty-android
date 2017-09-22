package jp.trackparty.android.destination;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.github.yusukeiwaki.realm_java_helper.RealmHelper;
import io.realm.Realm;
import jp.trackparty.android.R;
import jp.trackparty.android.api.TrackpartyApi;
import jp.trackparty.android.base.BaseAuthActivity;
import jp.trackparty.android.data.realm.Destination;
import jp.trackparty.android.data.realm.OngoingTransport;
import jp.trackparty.android.data.realm.TransportItem;
import jp.trackparty.android.databinding.ActivityDestinationDetailBinding;
import jp.trackparty.android.main.MainActivity;

/**
 * Destinationの詳細画面.
 */
public class DestinationDetailActivity extends BaseAuthActivity implements OnMapReadyCallback {
    private static final String TAG = DestinationDetailActivity.class.getSimpleName();
    private static final String PARAM_DESTINATION_ID = "destination_id";
    private long destinationId;
    private ActivityDestinationDetailBinding binding;
    private DestinationObserver destinationObserver;
    private OngoingTransportExistenceObserver ongoingTransportExistenceObserver;
    private DestinationGoogleMapManager googleMapManager;

    public static Intent newIntent(Context context, long destinationId) {
        Intent intent = new Intent(context, DestinationDetailActivity.class);
        intent.putExtra(PARAM_DESTINATION_ID, destinationId);
        return intent;
    }

    private void handleParams() {
        Intent intent = getIntent();
        if (intent == null) return;

        destinationId = intent.getLongExtra(PARAM_DESTINATION_ID, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleParams();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_destination_detail);

        setupGoogleMap();

        destinationObserver = new DestinationObserver(destinationId);
        destinationObserver.setCallback(new DestinationObserver.Callback() {
            @Override
            public void onUpdateDestination(@Nullable Destination destination) {
                binding.setDestination(destination);

                if (destination != null) {
                    googleMapManager.setCenter(destination.latitude, destination.longitude);
                }
            }
        });

        ongoingTransportExistenceObserver = new OngoingTransportExistenceObserver();
        ongoingTransportExistenceObserver.setCallback(new OngoingTransportExistenceObserver.Callback() {
            @Override
            public void onUpdateOngoingTransportExistence(boolean exists) {
                if (exists) {
                    binding.btnSelectDestinationContainer.setVisibility(View.GONE);
                } else {
                    binding.btnSelectDestinationContainer.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.btnSelectDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransportItem transportItem = RealmHelper.getInstance().executeTransactionForRead(new RealmHelper.TransactionForRead<TransportItem>() {
                    @Override
                    public TransportItem execute(Realm realm) throws Exception {
                        return realm.where(TransportItem.class).equalTo("destination.id", destinationId).findFirst();
                    }
                });
                if (transportItem != null) {
                    setOngoingTransport(transportItem.id);
                }
            }
        });

        if (savedInstanceState == null) {
            DestinationDetailViewModel.forceScheduleUpdate(destinationId);
            DestinationDetailService.start(this);
        }
    }

    private void setupGoogleMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);
        googleMapManager = new DestinationGoogleMapManager();
    }

    private void setOngoingTransport(final long transportItemId) {
        // TODO ログインなどと同様、APIコールはサービスに切り出して、ViewModelを通してLoadingを出す/消すようにすべき
        showOrHideProgress(true);
        TrackpartyApi.newInstance(this).startTransportItem(transportItemId)
                .onSuccessTask(new Continuation<JSONObject, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<JSONObject> task) throws Exception {
                        return OngoingTransport.setCurrentTransportItem(transportItemId);
                    }
                })
                .onSuccess(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        Intent intent = MainActivity.newIntent(DestinationDetailActivity.this);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        return null;
                    }
                })
                .continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {
                        if (task.isFaulted()) {
                            showOrHideProgress(false);
                            Exception e = task.getError();
                            Toast.makeText(DestinationDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, e.getMessage(), e);
                        }
                        return null;
                    }
                });
    }

    private void showOrHideProgress(boolean show) {
        binding.progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        destinationObserver.subscribe();
        ongoingTransportExistenceObserver.subscribe();
    }

    @Override
    protected void onPause() {
        ongoingTransportExistenceObserver.unsubscribe();
        destinationObserver.unsubscribe();
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapManager.onMapReady(googleMap);
    }
}
