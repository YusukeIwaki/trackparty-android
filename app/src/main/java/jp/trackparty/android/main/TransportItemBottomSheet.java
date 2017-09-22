package jp.trackparty.android.main;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;
import android.widget.TextView;

import jp.trackparty.android.R;
import jp.trackparty.android.data.realm.TransportItem;

class TransportItemBottomSheet {
    private final View rootView;
    private final TextView btnOngoingTransportItem;
    private final View ongoingTransportContainer;
    private final BottomSheetBehavior bottomSheetBehavior;
    private TransportItem ongoingTransportItem;

    public interface OnDestinationClickListener {
        void onDestinationClick(TransportItem transportItem);
    }

    private OnDestinationClickListener onDestinationClickListener;

    public void setOnDestinationClickListener(OnDestinationClickListener onDestinationClickListener) {
        this.onDestinationClickListener = onDestinationClickListener;
    }

    public interface OnActionClickListener {
        void onActionClick(OngoingTransportItemAction action);
    }

    private OnActionClickListener onActionClickListener;

    public void setOnActionClickListener(OnActionClickListener onActionClickListener) {
        this.onActionClickListener = onActionClickListener;
    }

    public TransportItemBottomSheet(View bottomSheetView) {
        rootView = bottomSheetView;
        btnOngoingTransportItem = rootView.findViewById(R.id.btn_ongoing_transport_item);
        ongoingTransportContainer = rootView.findViewById(R.id.ongoing_transport_container);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        setupBottomSheet();
        setupClickListeners();
    }

    private void setupBottomSheet() {
        btnOngoingTransportItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // slideOffset:
                //  0->collapsed
                //  1->expanded

                float alpha = (float) Math.sqrt(slideOffset);
                setTranslationAlpha(alpha);
            }
        });
    }

    public void initialize() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            setTranslationAlpha(1);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            setTranslationAlpha(0);
        }
    }

    private void setTranslationAlpha(float alpha) {
        ongoingTransportContainer.setAlpha(alpha);
        ongoingTransportContainer.setVisibility(alpha == 0 ? View.INVISIBLE : View.VISIBLE);
        btnOngoingTransportItem.setAlpha(1-alpha);
        btnOngoingTransportItem.setVisibility(alpha == 1 ? View.INVISIBLE : View.VISIBLE);
    }

    public void setOngoingTransportitem(@Nullable TransportItem transportItem) {
        ongoingTransportItem = transportItem;
    }

    private void setupClickListeners() {
        ongoingTransportContainer.findViewById(R.id.btn_show_destination_detail).setOnClickListener(onClickListener);
        ongoingTransportContainer.findViewById(R.id.fab_arrived).setOnClickListener(onFabClickListener);
        ongoingTransportContainer.findViewById(R.id.fab_rest).setOnClickListener(onFabClickListener);
        ongoingTransportContainer.findViewById(R.id.fab_waiting).setOnClickListener(onFabClickListener);
        ongoingTransportContainer.findViewById(R.id.fab_running).setOnClickListener(onFabClickListener);
        ongoingTransportContainer.findViewById(R.id.fab_misc).setOnClickListener(onFabClickListener);
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (onDestinationClickListener != null && ongoingTransportItem != null) {
                onDestinationClickListener.onDestinationClick(ongoingTransportItem);
            }
        }
    };

    private final View.OnClickListener onFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (onActionClickListener != null) {
                onActionClickListener.onActionClick(getOngoingTransportActionFor(view.getId()));
            }
        }
    };

    private OngoingTransportItemAction getOngoingTransportActionFor(int id) {
        switch (id) {
            case R.id.fab_arrived: return OngoingTransportItemAction.ARRIVED;
            case R.id.fab_rest: return OngoingTransportItemAction.REST;
            case R.id.fab_waiting: return OngoingTransportItemAction.WAITING;
            case R.id.fab_running: return OngoingTransportItemAction.RUNNING;
            case R.id.fab_misc: return OngoingTransportItemAction.MISC;
        }
        throw new IllegalArgumentException("unknown id: " + id);
    }

    public boolean collapseIfNeeded() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return true;
        }
        return false;
    }
}
