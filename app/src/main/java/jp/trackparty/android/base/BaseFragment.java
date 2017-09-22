package jp.trackparty.android.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import jp.trackparty.android.R;

public abstract class BaseFragment extends Fragment {

    protected abstract int getLayout();

    protected abstract void onSetupView();

    protected View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayout(), container, false);
        onSetupView();
        return rootView;
    }

    protected void showFragment(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    protected void showFragmentWithBackStack(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {
                return AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
            } else {
                return AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);
            }
        }

        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
            if (enter) {
                return AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
            } else {
                return AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_left);
            }
        }

        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    protected void finish() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        }
    }
}
