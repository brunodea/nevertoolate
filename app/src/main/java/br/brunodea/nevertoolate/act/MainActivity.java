package br.brunodea.nevertoolate.act;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDB;
import br.brunodea.nevertoolate.frag.HomeFragment;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.view.BottomNavigationViewBehavior;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnHomeFragmentListener {
    private static final String TAG = "MainActivity";

    @BindView(R.id.toolbar) android.support.v7.widget.Toolbar mToolbar;
    @BindView(R.id.navigation) BottomNavigationView mBottomNavigationView;
    @BindView(R.id.cl_main_layout) CoordinatorLayout mCLMainLayout;

    private BottomNavigationViewBehavior mBNVBehavior;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        //TODO
                        return true;
                    case R.id.navigation_favorites:
                        //TODO
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
            setHomeFragment();
        }
    }

    private void setHomeFragment() {
        FragmentTransaction ftrs = getSupportFragmentManager().beginTransaction();

        HomeFragment homeFragment = HomeFragment.newInstance();
        ftrs.replace(R.id.fl_fragment_container, homeFragment);

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
}
