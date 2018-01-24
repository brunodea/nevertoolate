package br.brunodea.nevertoolate.act;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.frag.HomeFragment;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.view.BottomNavigationViewBehavior;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnHomeFragmentListener {

    @BindView(R.id.toolbar) android.support.v7.widget.Toolbar mToolbar;
    @BindView(R.id.navigation) BottomNavigationView mBottomNavigationView;

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

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mBottomNavigationView.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationViewBehavior());

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
    public void onActionFavorite(SubmissionParcelable submission) {
        Toast.makeText(this, "FAVORITE!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionShare(SubmissionParcelable submission) {
        Toast.makeText(this, "SHARE!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionReddit(SubmissionParcelable submission) {
        Toast.makeText(this, "REDDIT!", Toast.LENGTH_SHORT).show();
    }
}
