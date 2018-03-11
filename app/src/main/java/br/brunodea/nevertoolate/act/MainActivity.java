package br.brunodea.nevertoolate.act;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.google.firebase.analytics.FirebaseAnalytics;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDBHelper;
import br.brunodea.nevertoolate.frag.FavoritesFragment;
import br.brunodea.nevertoolate.frag.HomeFragment;
import br.brunodea.nevertoolate.frag.NotificationsFragment;
import br.brunodea.nevertoolate.model.ListingSubmissionParcelable;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String ARG_CURR_SCREEN = "arg-curr-screen";
    private static final String ARG_HOME_SUBMISSIONS = "arg-home-submissions";

    @BindView(R.id.toolbar) android.support.v7.widget.Toolbar mToolbar;
    @BindView(R.id.appbar) AppBarLayout mAppBar;
    @BindView(R.id.navigation) BottomNavigationView mBottomNavigationView;
    @BindView(R.id.cl_main_layout) CoordinatorLayout mCLMainLayout;
    @BindView(R.id.fab) FloatingActionButton mFAB;

    /* The values for the constants below should follow the ordinal order of their counter-parts
     * in the Screen enum.
     */
    private static final int SCREEN_HOME = 0;
    private static final int SCREEN_FAVORITES = 1;
    private static final int SCREEN_NOTIFICATIONS = 2;
    public enum Screen {
        HOME,
        FAVORITES,
        NOTIFICATIONS
    }

    private Screen mCurrScreen;
    private ListingSubmissionParcelable mHomeListingSubmissionsParcelable;
    private DefaultSubmissionCardListener mDefaultSubmissionCardListener;
    private NeverTooLateUtil.AnalyticsListener mAnalyticsListener;

    private HomeFragment mHomeFragment;
    private FavoritesFragment mFavoritesFragment;
    private NotificationsFragment mNotificationFragment;

    private FirebaseAnalytics mFirebaseAnalytics;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                // make sure AppBar is expanded whenever changing fragments.
                mAppBar.setExpanded(true, true);
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

        {
            /*
            * Just so the upgrade to the new DB takes place.
            */
            NeverTooLateDBHelper ntl = new NeverTooLateDBHelper(this);
            ntl.getReadableDatabase();
        }

        mHomeFragment = null;
        mFavoritesFragment = null;
        mNotificationFragment = null;

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAnalyticsListener = (event_name, params) -> {
            Bundle bundle = new Bundle();
            for (Pair<String, String> p : params) {
                bundle.putString(p.first, p.second);
            }
            mFirebaseAnalytics.logEvent(event_name, bundle);
        };

        setSupportActionBar(mToolbar);
        mDefaultSubmissionCardListener = new DefaultSubmissionCardListener(this, mCLMainLayout);

        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        int screen_ordinal = Screen.HOME.ordinal();
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                // this activity may be opened via some notification
                screen_ordinal = intent.getIntExtra(ARG_CURR_SCREEN, 0);
            } else {
                // Defaults to home fragment
                mHomeListingSubmissionsParcelable = null;
            }
        } else {
            // It is important to set the home listing submission before calling
            // setHomeFragment(), which can be here onCreate or somewhere else.
            if (savedInstanceState.containsKey(ARG_HOME_SUBMISSIONS)) {
                mHomeListingSubmissionsParcelable = savedInstanceState.getParcelable(ARG_HOME_SUBMISSIONS);
            }
            if (savedInstanceState.containsKey(ARG_CURR_SCREEN)) {
                screen_ordinal = savedInstanceState.getInt(ARG_CURR_SCREEN);
            }
        }

        switch (screen_ordinal) {
            case SCREEN_HOME:
                setHomeFragment();
                mBottomNavigationView.setSelectedItemId(R.id.navigation_home);
                break;
            case SCREEN_FAVORITES:
                setFavoritesFragment();
                mBottomNavigationView.setSelectedItemId(R.id.navigation_favorites);
                break;
            case SCREEN_NOTIFICATIONS:
                setNotificationFragment();
                mBottomNavigationView.setSelectedItemId(R.id.navigation_notifications);
                break;
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

    private void showScreen(Screen screen) {
        mCurrScreen = screen;
        FragmentTransaction ftrs = getSupportFragmentManager().beginTransaction();

        if (mHomeFragment == null) {
            mHomeFragment = HomeFragment.newInstance(mHomeListingSubmissionsParcelable);
            mHomeFragment.setSubmissionCardListener(mDefaultSubmissionCardListener);
            mHomeFragment.setRetainInstance(true);
            mHomeFragment.setAnalyticsListener(mAnalyticsListener);
            ftrs.add(R.id.fl_fragment_container, mHomeFragment);
        }
        ftrs.hide(mHomeFragment);

        if (mFavoritesFragment == null) {
            mFavoritesFragment = FavoritesFragment.newInstance();
            mFavoritesFragment.setSubmissionCardListener(mDefaultSubmissionCardListener);
            mFavoritesFragment.setRetainInstance(true);
            mFavoritesFragment.setAnalyticsListener(mAnalyticsListener);
            ftrs.add(R.id.fl_fragment_container, mFavoritesFragment);
        }
        ftrs.hide(mFavoritesFragment);

        if (mNotificationFragment == null) {
            mNotificationFragment = NotificationsFragment.newInstance();
            mNotificationFragment.setRetainInstance(true);
            mNotificationFragment.setAnalyticsListener(mAnalyticsListener);
            ftrs.add(R.id.fl_fragment_container, mNotificationFragment);
        }
        ftrs.hide(mNotificationFragment);

        switch (screen.ordinal()) {
            case SCREEN_HOME:
                ftrs.show(mHomeFragment);
                if (mFAB.getVisibility() == View.VISIBLE) {
                    mFAB.hide();
                }
                break;
            case SCREEN_FAVORITES:
                ftrs.show(mFavoritesFragment);
                if (mFAB.getVisibility() == View.VISIBLE) {
                    mFAB.hide();
                }
                break;
            case SCREEN_NOTIFICATIONS:
                ftrs.show(mNotificationFragment);
                if (mFAB.getVisibility() != View.VISIBLE) {
                    mFAB.show();
                }
                mFAB.setOnClickListener(view -> mNotificationFragment.onFabClick());
                break;
        }

        ftrs.commit();
    }

    private void setHomeFragment() {
        showScreen(Screen.HOME);
    }

    private void setFavoritesFragment() {
        showScreen(Screen.FAVORITES);
    }

    private void setNotificationFragment() {
        showScreen(Screen.NOTIFICATIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "On Activity Result request code: " + requestCode);
        if (mNotificationFragment != null) {
            Log.d(TAG, "On Activity Result: Notification Fragment not null!");
            if (requestCode == NotificationsFragment.NOTIFICATION_PLACE_PICKER_REQUEST) {
                Log.d(TAG, "On Activity Result: Notification Fragment: request code for place_picker!");
                mNotificationFragment.onPlacePickerResult(resultCode, data);
            }
        }
    }
}
