package br.brunodea.nevertoolate.act;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDB;
import br.brunodea.nevertoolate.frag.FavoritesFragment;
import br.brunodea.nevertoolate.frag.HomeFragment;
import br.brunodea.nevertoolate.frag.SubmissionCardListener;
import br.brunodea.nevertoolate.model.ListingSubmissionParcelable;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.view.BottomNavigationViewBehavior;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SubmissionCardListener {
    private static final String TAG = "MainActivity";
    private static final String ARG_CURR_SCREEN = "arg-curr-screen";

    @BindView(R.id.toolbar) android.support.v7.widget.Toolbar mToolbar;
    @BindView(R.id.navigation) BottomNavigationView mBottomNavigationView;
    @BindView(R.id.cl_main_layout) CoordinatorLayout mCLMainLayout;

    private BottomNavigationViewBehavior mBNVBehavior;

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
                        //TODO
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
        mBNVBehavior = new BottomNavigationViewBehavior();

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mBottomNavigationView.getLayoutParams();
        layoutParams.setBehavior(mBNVBehavior);

        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (savedInstanceState == null) {
            mHomeListingSubmissionsParcelable = null;
            setHomeFragment();
        } else {
            switch (savedInstanceState.getInt(ARG_CURR_SCREEN)) {
                case SCREEN_HOME:
                    setHomeFragment();
                    break;
                case SCREEN_FAVORITES:
                    setFavoritesFragment();
                    break;
                case SCREEN_NOTIFICATIONS:
                    mCurrScreen = Screen.NOTIFICATIONS;
                    // TODO: change to notifications screen
                    setHomeFragment();
                    break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_CURR_SCREEN, mCurrScreen.ordinal());
        super.onSaveInstanceState(outState);
    }

    public void setHomeSubmissions(ListingSubmissionParcelable submissions) {
        mHomeListingSubmissionsParcelable = submissions;
    }

    private void setHomeFragment() {
        mCurrScreen = Screen.HOME;
        FragmentTransaction ftrs = getSupportFragmentManager().beginTransaction();


        HomeFragment homeFragment = HomeFragment.newInstance(mHomeListingSubmissionsParcelable);
        ftrs.replace(R.id.fl_fragment_container, homeFragment);

        ftrs.commit();
    }

    private void setFavoritesFragment() {
        mCurrScreen = Screen.FAVORITES;
        FragmentTransaction ftrs = getSupportFragmentManager().beginTransaction();

        FavoritesFragment favoritesFragment = FavoritesFragment.newInstance();
        ftrs.replace(R.id.fl_fragment_container, favoritesFragment);

        ftrs.commit();
    }

    @Override
    public boolean onActionFavorite(SubmissionParcelable submission) {
        if (NeverTooLateDB.isFavorite(this, submission)) {
            NeverTooLateDB.deleteSubmission(this, submission);
            return false;
        } else {
            NeverTooLateDB.insertSubmission(this, submission);
            return true;
        }
    }

    @Override
    public void onActionShare(SubmissionParcelable submission, Uri bitmapUri) {
        if (bitmapUri != null) {
            Log.i(MainActivity.TAG, "Sharing: " + bitmapUri);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image_title)));
        } else {
            mBNVBehavior.slideDown(mBottomNavigationView);
            Snackbar.make(mCLMainLayout, getString(R.string.share_error),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActionReddit(SubmissionParcelable submission) {
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://reddit.com" + submission.permalink())
        );
        startActivity(intent);
    }

    @Override
    public void onImageClick(ImageView imageView, SubmissionParcelable submission) {
        Intent intent = new Intent(this, FullscreenImageActivity.class);
        intent.putExtra(FullscreenImageActivity.ARG_SUBMISSION, submission);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, imageView, getString(R.string.fullscreenImageViewTransition));
        startActivity(intent, options.toBundle());
    }
}
