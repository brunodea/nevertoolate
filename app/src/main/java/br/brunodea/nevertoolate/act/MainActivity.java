package br.brunodea.nevertoolate.act;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.frag.FavoritesFragment;
import br.brunodea.nevertoolate.frag.HomeFragment;
import br.brunodea.nevertoolate.frag.NotificationsFragment;
import br.brunodea.nevertoolate.model.ListingSubmissionParcelable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String ARG_CURR_SCREEN = "arg-curr-screen";
    private static final String ARG_HOME_SUBMISSIONS = "arg-home-submissions";

    @BindView(R.id.toolbar) android.support.v7.widget.Toolbar mToolbar;
    @BindView(R.id.navigation) BottomNavigationView mBottomNavigationView;
    @BindView(R.id.cl_main_layout) CoordinatorLayout mCLMainLayout;
    @BindView(R.id.fab) FloatingActionButton mFAB;

    /* The values for the constants below should follow the ordinal order of their counter-parts
     * in the Screen enum.
     */
    private static final int SCREEN_HOME = 0;
    private static final int SCREEN_FAVORITES = 1;
    private static final int SCREEN_NOTIFICATIONS = 2;
    private enum Screen {
        HOME,
        FAVORITES,
        NOTIFICATIONS
    }

    private Screen mCurrScreen;
    private ListingSubmissionParcelable mHomeListingSubmissionsParcelable;
    private DefaultSubmissionCardListener mDefaultSubmissionCardListener;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        if (mCurrScreen != Screen.HOME) {
                            setHomeFragment();
                        }
                        return true;
                    case R.id.navigation_favorites:
                        if (mCurrScreen != Screen.FAVORITES) {
                            setFavoritesFragment();
                        }
                        return true;
                    case R.id.navigation_notifications:
                        if (mCurrScreen != Screen.NOTIFICATIONS) {
                            setNotificationFragment();
                        }
                        return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mDefaultSubmissionCardListener = new DefaultSubmissionCardListener(this, mCLMainLayout);

        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (savedInstanceState == null) {
            mHomeListingSubmissionsParcelable = null;
            setHomeFragment();
        } else {
            // It is important to set the home listing submission before calling
            // setHomeFragment(), which can be here onCreate or somewhere else.
            if (savedInstanceState.containsKey(ARG_HOME_SUBMISSIONS)) {
                mHomeListingSubmissionsParcelable = savedInstanceState.getParcelable(ARG_HOME_SUBMISSIONS);
            }
            if (savedInstanceState.containsKey(ARG_CURR_SCREEN)) {
                switch (savedInstanceState.getInt(ARG_CURR_SCREEN)) {
                    case SCREEN_HOME:
                        setHomeFragment();
                        break;
                    case SCREEN_FAVORITES:
                        setFavoritesFragment();
                        break;
                    case SCREEN_NOTIFICATIONS:
                        setNotificationFragment();
                        break;
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_CURR_SCREEN, mCurrScreen.ordinal());
        if (mHomeListingSubmissionsParcelable != null) {
            outState.putParcelable(ARG_HOME_SUBMISSIONS, mHomeListingSubmissionsParcelable);
        }
        super.onSaveInstanceState(outState);
    }

    public void setHomeSubmissions(ListingSubmissionParcelable submissions) {
        mHomeListingSubmissionsParcelable = submissions;
    }

    private void setHomeFragment() {
        mCurrScreen = Screen.HOME;
        FragmentTransaction ftrs = getSupportFragmentManager().beginTransaction();

        HomeFragment homeFragment = HomeFragment.newInstance(mHomeListingSubmissionsParcelable);
        homeFragment.setSubmissionCardListener(mDefaultSubmissionCardListener);
        ftrs.replace(R.id.fl_fragment_container, homeFragment);
        if (mFAB.getVisibility() == View.VISIBLE) {
            mFAB.hide();
        }

        ftrs.commit();
    }

    private void setFavoritesFragment() {
        mCurrScreen = Screen.FAVORITES;
        FragmentTransaction ftrs = getSupportFragmentManager().beginTransaction();

        FavoritesFragment favoritesFragment = FavoritesFragment.newInstance();
        favoritesFragment.setSubmissionCardListener(mDefaultSubmissionCardListener);
        ftrs.replace(R.id.fl_fragment_container, favoritesFragment);
        if (mFAB.getVisibility() == View.VISIBLE) {
            mFAB.hide();
        }

        ftrs.commit();
    }

    private void setNotificationFragment() {
        mCurrScreen = Screen.NOTIFICATIONS;
        FragmentTransaction ftrs = getSupportFragmentManager().beginTransaction();

        NotificationsFragment notificationsFragment = NotificationsFragment.newInstance();
        ftrs.replace(R.id.fl_fragment_container, notificationsFragment);
        if (mFAB.getVisibility() != View.VISIBLE) {
            mFAB.show();
        }
        mFAB.setOnClickListener(view -> notificationsFragment.onFabClick());

        ftrs.commit();
    }
}
