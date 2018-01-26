package br.brunodea.nevertoolate.frag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.act.FullscreenImageActivity;
import br.brunodea.nevertoolate.model.ListingSubmissionParcelable;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;
import br.brunodea.nevertoolate.util.RedditUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnHomeFragmentListener}
 * interface.
 */
public class HomeFragment extends Fragment {
    private static final String BUNDLE_LISTING_SUBMISSION_PARCELABLE = "listing-submission-parcelable";

    private OnHomeFragmentListener mFragmentInteractionListener;

    @BindView(R.id.rv_posts) RecyclerView mRecyclerView;
    @BindView(R.id.fl_posts_container) FrameLayout mFLPostsContainer;
    @BindView(R.id.tv_error_message) TextView mTVErrorMessage;
    @BindView(R.id.swiperefresh) SwipeRefreshLayout mSwipeRefreshLayout;

    private HomeRecyclerViewAdapter mHomeRecyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mHomeRecyclerViewAdapter.getRedditPosts() != null && mHomeRecyclerViewAdapter.getRedditPosts().size() > 0) {
            outState.putParcelable(
                    BUNDLE_LISTING_SUBMISSION_PARCELABLE,
                    mHomeRecyclerViewAdapter.getRedditPosts()
            );
        }
        super.onSaveInstanceState(outState);
    }

    public void refreshRecyclerView() {
        mSwipeRefreshLayout.setRefreshing(true);
        RedditUtils.queryGetMotivated(submissions -> {
            mTVErrorMessage.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mHomeRecyclerViewAdapter.setRedditPosts(new ListingSubmissionParcelable(submissions));
            mSwipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.submission_list, container, false);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        if (mHomeRecyclerViewAdapter == null) {
            mHomeRecyclerViewAdapter = new HomeRecyclerViewAdapter(
                    getContext(),
                    mFragmentInteractionListener,
                    (imageView, submission) -> {
                        Intent intent = new Intent(getActivity(), FullscreenImageActivity.class);
                        intent.putExtra(FullscreenImageActivity.ARG_SUBMISSION, submission);
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(getActivity(), imageView, getString(R.string.fullscreenImageViewTransition));
                        startActivity(intent, options.toBundle());
                    }
            );
            mRecyclerView.setAdapter(mHomeRecyclerViewAdapter);
        }

        boolean is_tablet = NeverTooLateUtil.isTablet(getContext());
        boolean is_land = NeverTooLateUtil.isLandscape(getContext());
        int columns = is_tablet ? (is_land ? 4 : 2) : (is_land ? 2 : 1);
        if (columns <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            StaggeredGridLayoutManager mgr = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
            mgr.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
            mRecyclerView.setLayoutManager(mgr);
        }
        mRecyclerView.setHasFixedSize(false);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_LISTING_SUBMISSION_PARCELABLE)) {
            mHomeRecyclerViewAdapter.setRedditPosts(
                    savedInstanceState.getParcelable(BUNDLE_LISTING_SUBMISSION_PARCELABLE)
            );
            mRecyclerView.setVisibility(View.VISIBLE);
        }

        if (mHomeRecyclerViewAdapter.getRedditPosts() == null || mHomeRecyclerViewAdapter.getRedditPosts().size() == 0) {
            if (NeverTooLateUtil.isOnline(getContext())) {
                refreshRecyclerView();
            } else {
                mRecyclerView.setVisibility(View.GONE);
                mTVErrorMessage.setVisibility(View.VISIBLE);
            }
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_home_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_home_refresh:
                refreshRecyclerView();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHomeFragmentListener) {
            mFragmentInteractionListener = (OnHomeFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnHomeFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentInteractionListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnHomeFragmentListener {
        void onActionFavorite(SubmissionParcelable submission);
        void onActionShare(SubmissionParcelable submission);
        void onActionReddit(SubmissionParcelable submission);
    }
}
