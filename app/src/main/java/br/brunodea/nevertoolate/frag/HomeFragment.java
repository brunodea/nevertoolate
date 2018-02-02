package br.brunodea.nevertoolate.frag;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.act.MainActivity;
import br.brunodea.nevertoolate.frag.list.SubmissionCardListener;
import br.brunodea.nevertoolate.frag.list.SubmissionRecyclerViewAdapter;
import br.brunodea.nevertoolate.model.ListingSubmissionParcelable;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;
import br.brunodea.nevertoolate.util.RedditUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link SubmissionCardListener}
 * interface.
 */
public class HomeFragment extends Fragment {
    private static final String BUNDLE_LISTING_SUBMISSION_PARCELABLE = "listing-submission-parcelable";

    private SubmissionRecyclerViewAdapter mSubmissionRecyclerViewAdater;
    private SubmissionCardListener mSubmissionCardListener;
    private ListingSubmissionParcelable mListingSubmissionParcelable;

    @BindView(R.id.cl_submission_list_content) ConstraintLayout mCLMain;
    @BindView(R.id.rv_posts) RecyclerView mRecyclerView;
    @BindView(R.id.tv_error_message) TextView mTVErrorMessage;
    @BindView(R.id.swiperefresh) SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HomeFragment() {
    }

    public static HomeFragment newInstance(ListingSubmissionParcelable listingSubmissionParcelable) {
        HomeFragment res = new HomeFragment();
        if (listingSubmissionParcelable != null && !listingSubmissionParcelable.isEmpty()) {
            Bundle args = new Bundle();
            args.putParcelable(BUNDLE_LISTING_SUBMISSION_PARCELABLE, listingSubmissionParcelable);
            res.setArguments(args);
        }

        return res;
    }

    public void setSubmissionCardListener(SubmissionCardListener listener) {
        mSubmissionCardListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListingSubmissionParcelable = null;
        Bundle args = getArguments();
        if (args != null && args.containsKey(BUNDLE_LISTING_SUBMISSION_PARCELABLE)) {
            mListingSubmissionParcelable = args.getParcelable(BUNDLE_LISTING_SUBMISSION_PARCELABLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mListingSubmissionParcelable != null && !mListingSubmissionParcelable.isEmpty()) {
            outState.putParcelable(BUNDLE_LISTING_SUBMISSION_PARCELABLE, mListingSubmissionParcelable);
        }
        super.onSaveInstanceState(outState);
    }

    public void refreshRecyclerView() {
        mSwipeRefreshLayout.setRefreshing(true);
        if (NeverTooLateUtil.isOnline(getContext())) {
            RedditUtils.queryGetMotivated(submissions -> {
                mSwipeRefreshLayout.setRefreshing(false);
                if (submissions.isEmpty()) {
                    mTVErrorMessage.setText(R.string.loading_error);
                    mTVErrorMessage.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                } else {
                    mTVErrorMessage.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mListingSubmissionParcelable = new ListingSubmissionParcelable(submissions);
                    mSubmissionRecyclerViewAdater.setRedditPosts(mListingSubmissionParcelable);
                    updateMainActivityHomeSubmissions();
                }
            }, 10);
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(mCLMain, R.string.no_internet, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.submission_list, container, false);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        if (mSubmissionRecyclerViewAdater == null) {
            mSubmissionRecyclerViewAdater = new SubmissionRecyclerViewAdapter(mSubmissionCardListener);
            mRecyclerView.setAdapter(mSubmissionRecyclerViewAdater);
        }
        mSwipeRefreshLayout.setOnRefreshListener(() -> refreshRecyclerView());

        boolean is_tablet = NeverTooLateUtil.isTablet(getContext());
        boolean is_land = NeverTooLateUtil.isLandscape(getContext());
        int columns = is_tablet ? (is_land ? 4 : 2) : (is_land ? 2 : 1);
        if (columns <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            GridLayoutManager glm = new GridLayoutManager(getContext(), columns);
            mSubmissionRecyclerViewAdater.setFixedImageSize(getResources().getDimensionPixelSize(R.dimen.image_default_size));
            mRecyclerView.setLayoutManager(glm);
        }
        // false because the text in the bottom part of the card may vary its length
        mRecyclerView.setHasFixedSize(false);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_LISTING_SUBMISSION_PARCELABLE)) {
            mListingSubmissionParcelable = savedInstanceState.getParcelable(BUNDLE_LISTING_SUBMISSION_PARCELABLE);
        }

        if (mListingSubmissionParcelable != null) {
            mSubmissionRecyclerViewAdater.setRedditPosts(mListingSubmissionParcelable);
        }

        if (mSubmissionRecyclerViewAdater.getRedditPosts() == null || mSubmissionRecyclerViewAdater.getRedditPosts().size() == 0) {
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSubmissionCardListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        // make sure that whenever this fragment is replace()'d, the main activity keeps a track of
        // the loaded submissions.
        updateMainActivityHomeSubmissions();
    }

    void updateMainActivityHomeSubmissions() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setHomeSubmissions(mListingSubmissionParcelable);
        }
    }
}
