package jp.trackparty.android.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import jp.trackparty.android.R;

/**
 * Fragmentの親Activityのベースクラス
 *
 * 注意：Fragmentをいれるための @+id/fragment_container を含むレイアウトを保つ必要があります
 */
public abstract class BaseFragmentActivity extends BaseActivity {
    public interface OnBackPressListener {
        boolean onBackPressed();
    }

    protected void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    protected void showFragmentWithBackStack(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    protected void showFragments(Fragment[] fragments) {
        if (fragments.length == 0) return;
        if (fragments.length == 1) {
            showFragment(fragments[0]);
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragments[0]);
        for (int i = 1; i < fragments.length; i++) {
            transaction.add(R.id.fragment_container, fragments[i]);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(null);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null && fragment instanceof OnBackPressListener && ((OnBackPressListener) fragment).onBackPressed()) {
            return;
        }

        super.onBackPressed();
    }
}
