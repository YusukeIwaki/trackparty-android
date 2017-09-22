package jp.trackparty.android.main;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;

import jp.trackparty.android.data.realm.User;
import jp.trackparty.android.databinding.DrawerHeaderBinding;

public class NavigationViewManager {
    private final NavigationView navigationView;
    private final CurrentUserObserver currentUserObserver;
    private final DrawerHeaderBinding navHeaderViewBinding;

    public interface OnMenuItemClickListener {
        boolean onMenuItemClicked(MenuItem item);
    }

    private OnMenuItemClickListener onMenuItemClickListener;

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    public NavigationViewManager(NavigationView navigationView) {
        this.navigationView = navigationView;
        if (navigationView.getHeaderCount() == 0) throw new IllegalArgumentException("navigationView should have a header");
        navHeaderViewBinding = DataBindingUtil.bind(navigationView.getHeaderView(0));
        currentUserObserver = new CurrentUserObserver();
        setupListeners();
    }

    private void setupListeners() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (onMenuItemClickListener != null) {
                    return onMenuItemClickListener.onMenuItemClicked(item);
                }
                return false;
            }
        });

        currentUserObserver.setCallback(new CurrentUserObserver.Callback() {
            @Override
            public void onUpdateUser(User user) {
                if (navigationView.getHeaderCount() > 0) {
                    navHeaderViewBinding.setUser(user);
                }
            }
        });
    }

    public void enable() {
        currentUserObserver.subscribe();
    }

    public void disable() {
        currentUserObserver.unsubscribe();
    }
}
