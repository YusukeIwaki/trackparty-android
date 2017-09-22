package jp.trackparty.android.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import bolts.Continuation;
import bolts.Task;
import jp.trackparty.android.R;
import jp.trackparty.android.api.TrackpartyApi;
import jp.trackparty.android.arrival.ArrivalActivity;
import jp.trackparty.android.background_fetch.BackgroundFetchTaskScheduling;
import jp.trackparty.android.base.BaseAuthActivity;
import jp.trackparty.android.data.realm.OngoingTransport;
import jp.trackparty.android.data.realm.TransportItem;
import jp.trackparty.android.databinding.ActivityMainContentBinding;
import jp.trackparty.android.destination.DestinationDetailActivity;
import jp.trackparty.android.positioning.PeriodecalPositioningService;
import jp.trackparty.android.positioning.PositioningRequirementCheckAndStartPositioningActivity;
import jp.trackparty.android.transport_item_list.TransportItemListActivity;
import jp.trackparty.api.ApiV1;

public class MainActivity extends BaseAuthActivity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private NavigationViewManager navigationViewManager;
    private GoogleMapManager googleMapManager;
    private ActivityMainContentBinding binding;
    private OngoingTransportItemObserver ongoingTransportItemObserver;
    private TransportItemBottomSheet transportItemBottomSheet;

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainContentBinding.bind(findViewById(R.id.coordinator_layout));

        setupNavigationView();
        setupGoogleMap();

        binding.btnPositioningNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMapManager.moveToCenterPosition();
                requestOneShotPositioning();
            }
        });
        binding.btnResetToCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleMapManager.moveToCenterPosition();
            }
        });
        binding.btnSelectTransportItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTransportItemListActivity();
            }
        });
        transportItemBottomSheet = new TransportItemBottomSheet(binding.bottomSheet);
        transportItemBottomSheet.setOnDestinationClickListener(new TransportItemBottomSheet.OnDestinationClickListener() {
            @Override
            public void onDestinationClick(TransportItem transportItem) {
                showDestinationDetailActivity(transportItem.destination.id);
            }
        });
        transportItemBottomSheet.setOnActionClickListener(new TransportItemBottomSheet.OnActionClickListener() {
            @Override
            public void onActionClick(final OngoingTransportItemAction action) {
                //TODO 本来はユーザー操作をブロックするのではなく、APIコールをService行い、ビューの更新はビューモデルを通して行うようにすべき。
                showOrHideProgressForTransporrItemAction(true);
                callApiFor(action).continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        showOrHideProgressForTransporrItemAction(false);
                        if (task.isFaulted()) {
                            Exception e = task.getError();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, e.getMessage(), e);
                        } else {
                            afterApiCallFor(action);
                        }
                        return null;
                    }
                });
            }
        });

        ongoingTransportItemObserver = new OngoingTransportItemObserver();
        ongoingTransportItemObserver.setCallback(new OngoingTransportItemObserver.Callback() {
            @Override
            public void onUpdateOngoingTransportItem(@Nullable TransportItem transportItem) {
                binding.setOngoingTransportItem(transportItem);
                transportItemBottomSheet.setOngoingTransportitem(transportItem);

                if (transportItem == null) {
                    stopPeriodicalPositioningService();
                    BackgroundFetchTaskScheduling.getInstance().enable();
                }
            }
        });

        if (savedInstanceState == null) {
            CurrentUserFetchService.start(this);
            requestOneShotPositioning();
            transportItemBottomSheet.initialize();

            keepAlivePeriodicalPositioningService();
        }
    }

    private void showOrHideProgressForTransporrItemAction(boolean show) {
        if (show) {
            binding.ongoingTransportContainer.setAlpha(0.4f);
        } else {
            binding.ongoingTransportContainer.setAlpha(1.0f);
        }

    }

    private Task<Void> callApiFor(OngoingTransportItemAction action) {
        final ApiV1 apiV1 = TrackpartyApi.newInstance(this);

        switch (action) {
            case ARRIVED:
                return apiV1.completeOngoingTransport().makeVoid();
            case REST:
            case WAITING:
                return apiV1.pauseOngoingTransport().makeVoid();
            case RUNNING:
                return apiV1.resumeOngoingTransport().makeVoid();
        }
        return Task.forResult(null);
    }

    private void afterApiCallFor(OngoingTransportItemAction action) {
        switch (action) {
            case ARRIVED:
                OngoingTransport.resetCurrentTransportItem();
                startActivity(ArrivalActivity.newIntent(MainActivity.this));
                break;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        transportItemBottomSheet.initialize();
    }

    private void showTransportItemListActivity() {
        startActivity(TransportItemListActivity.newIntent(this));
    }

    private void showDestinationDetailActivity(long destinationId) {
        startActivity(DestinationDetailActivity.newIntent(this, destinationId));
    }

    private void requestOneShotPositioning() {
        startActivity(PositioningRequirementCheckAndStartPositioningActivity.newIntent(this));
    }

    /**
     * onResumeで呼ぶと多分無限ループするので注意
     */
    private void keepAlivePeriodicalPositioningService() {
        if (OngoingTransport.getCurrentTransportItem() != null) {
            Intent intent = PositioningRequirementCheckAndStartPositioningActivity.newIntent(this, PeriodecalPositioningService.newIntentForStarting(this));
            startActivity(intent);
            BackgroundFetchTaskScheduling.getInstance().disable();
        } else {
            stopPeriodicalPositioningService();
            BackgroundFetchTaskScheduling.getInstance().enable();
        }
    }

    private void stopPeriodicalPositioningService() {
        startService(PeriodecalPositioningService.newIntentForStopping(this));
    }

    private void setupNavigationView() {
        navigationViewManager = new NavigationViewManager((NavigationView) findViewById(R.id.navigation));
        navigationViewManager.setOnMenuItemClickListener(new NavigationViewManager.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClicked(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.logout:
                        showLogoutDialog();
                        break;
                }

                return closeDrawerIfNeeded();
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("ログアウトしますか？")
                .setPositiveButton("ログアウト", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressDialog.show(MainActivity.this, null, "ログアウト中...", true, false);
                        LogoutService.start(MainActivity.this);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private boolean closeDrawerIfNeeded() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;
    }

    private void setupGoogleMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);

        googleMapManager = new GoogleMapManager();
        googleMapManager.setAutoTrackCenterStateCallback(new GoogleMapManager.AutoTrackCenterStateCallback() {
            @Override
            public void onUpdateAutoTrackCenterState(boolean autoTrackCenter) {
                binding.btnResetToCenter.setVisibility(autoTrackCenter ? View.GONE : View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationViewManager.enable();
        googleMapManager.enable();
        ongoingTransportItemObserver.subscribe();
    }

    @Override
    protected void onPause() {
        ongoingTransportItemObserver.unsubscribe();
        googleMapManager.disable();
        navigationViewManager.disable();
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapManager.onMapReady(googleMap);
    }

    @Override
    public void onBackPressed() {
        if (closeDrawerIfNeeded()) return;
        if (transportItemBottomSheet.collapseIfNeeded()) return;
        if (googleMapManager != null && binding.btnResetToCenter.getVisibility() == View.VISIBLE) {
            googleMapManager.moveToCenterPosition();
            return;
        }

        super.onBackPressed();
    }
}
