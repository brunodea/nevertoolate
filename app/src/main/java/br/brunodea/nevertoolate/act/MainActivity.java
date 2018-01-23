package br.brunodea.nevertoolate.act;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.dean.jraw.models.Submission;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.frag.HomeFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnListFragmentInteractionListener {

    @BindView(R.id.pb_loading_posts) ProgressBar mPBLoadingPosts;
    @BindView(R.id.toolbar) android.support.v7.widget.Toolbar mToolbar;

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
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (savedInstanceState == null) {
            setHomeFragment();
        }
    }

    private void setHomeFragment() {
        FragmentTransaction ftrs = getSupportFragmentManager().beginTransaction();

        HomeFragment frg = HomeFragment.newInstance(1);
        ftrs.replace(R.id.fl_fragment_container, frg);

        ftrs.commit();
    }

    @Override
    public void onStartLoadingPosts() {
        mPBLoadingPosts.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinishedLoadingPosts() {
        mPBLoadingPosts.setVisibility(View.GONE);
    }

    @Override
    public void onActionFavorite(Submission submission) {
        Toast.makeText(this, "FAVORITE!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionShare(Submission submission) {
        Toast.makeText(this, "SHARE!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionReddit(Submission submission) {
        Toast.makeText(this, "REDDIT!", Toast.LENGTH_SHORT).show();
    }
}
