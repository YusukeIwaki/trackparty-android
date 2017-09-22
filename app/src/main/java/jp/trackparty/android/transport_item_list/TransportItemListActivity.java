package jp.trackparty.android.transport_item_list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import bolts.Continuation;
import bolts.Task;
import jp.trackparty.android.R;
import jp.trackparty.android.base.BaseAuthActivity;
import jp.trackparty.android.data.realm.TransportItem;
import jp.trackparty.android.destination.DestinationDetailActivity;
import jp.trackparty.android.etc.TextUtils;

/**
 * 目的地を選択　の画面
 */
public class TransportItemListActivity extends BaseAuthActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, TransportItemListActivity.class);
    }

    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyState;
    private TransportItemListViewModelObserver transportItemListViewModelObserver;
    private OngoingTransportExistenceObserver ongoingTransportExistenceObserver;
    private TransportItemListAdapter transportItemListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transport_item_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestTransportItemList();
            }
        });
        emptyState = findViewById(R.id.empty_state);

        transportItemListAdapter = new TransportItemListAdapter();
        transportItemListAdapter.setOnTransportItemClickListener(new TransportItemListAdapter.OnTransportItemClickListener() {
            @Override
            public void onTransportItemClicked(TransportItem item) {
                showDestinationDetailActivity(item.destination.id);
            }
        });
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(transportItemListAdapter);

        ongoingTransportExistenceObserver = new OngoingTransportExistenceObserver();
        ongoingTransportExistenceObserver.setCallback(new OngoingTransportExistenceObserver.Callback() {
            @Override
            public void onUpdateOngoingTransportExistence(boolean exists) {
                if (exists) {
                    finish();
                }
            }
        });


        transportItemListViewModelObserver = new TransportItemListViewModelObserver();
        transportItemListViewModelObserver.setCallback(new TransportItemListViewModelObserver.Callback() {
            @Override
            public void onUpdateTransportItemListViewModel(@NonNull TransportItemListViewModel viewModel) {
                swipeRefreshLayout.setRefreshing(viewModel.isLoading());

                if (viewModel.state == TransportItemListViewModel.STATE_DONE) {
                    if (TextUtils.isEmpty(viewModel.lastError)) {
                        transportItemListAdapter.updateData(viewModel.transport_items);
                    } else {
                        Toast.makeText(TransportItemListActivity.this, viewModel.lastError, Toast.LENGTH_SHORT).show();
                    }

                    if (viewModel.transport_items.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                    }
                } else {
                    emptyState.setVisibility(View.GONE);
                }
            }
        });

        if (savedInstanceState == null) {
            requestTransportItemList();
        }
    }

    private void requestTransportItemList() {
        TransportItemListViewModel.forceScheduleUpdate().onSuccess(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                TransportItemListService.start(TransportItemListActivity.this);
                return null;
            }
        });
    }

    private void showDestinationDetailActivity(long destinationId) {
        startActivity(DestinationDetailActivity.newIntent(this, destinationId));
    }

    @Override
    protected void onResume() {
        super.onResume();
        ongoingTransportExistenceObserver.subscribe();
        transportItemListViewModelObserver.subscribe();
    }

    @Override
    protected void onPause() {
        transportItemListViewModelObserver.unsubscribe();
        ongoingTransportExistenceObserver.unsubscribe();
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
