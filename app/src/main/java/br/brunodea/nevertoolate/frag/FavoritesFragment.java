
package br.brunodea.nevertoolate.frag;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.frag.list.FavoritesListViewModel;
import br.brunodea.nevertoolate.frag.list.MotivationsAdapter;
import br.brunodea.nevertoolate.frag.list.SubmissionCardListener;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoritesFragment extends Fragment {
    private static final int LOADER_ID = 1;
    public static final String EXTRA_FAVORITE_POSITION = "extra-favorite-position";

    private MotivationsAdapter mFavoritesAdapter;

    private SubmissionCardListener mSubmissionListener;

    @BindView(R.id.rv_posts) RecyclerView mRecyclerView;
    @BindView(R.id.tv_error_message) TextView mTVErrorMessage;
    @BindView(R.id.swiperefresh) SwipeRefreshLayout mSwipeRefreshLayout;

    private NeverTooLateUtil.AnalyticsListener mAnalyticsListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavoritesFragment() {
    }

    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.submission_list, container, false);
        ButterKnife.bind(this, view);

        mSwipeRefreshLayout.setEnabled(false);
        NeverTooLateDatabase db = NeverTooLateDatabase.getInstance(getContext());
        mFavoritesAdapter = new MotivationsAdapter(new ArrayList<>(),
                db,
                mSubmissionListener,
                mAnalyticsListener);

        setHasOptionsMenu(false);

        boolean is_tablet = NeverTooLateUtil.isTablet(getContext());
        boolean is_land = NeverTooLateUtil.isLandscape(getContext());
        int columns = is_tablet ? (is_land ? 4 : 2) : (is_land ? 2 : 1);
        if (columns <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            GridLayoutManager glm = new GridLayoutManager(getContext(), columns);
            mFavoritesAdapter.setFixedImageSize(getResources().getDimensionPixelSize(R.dimen.image_default_size));
            mRecyclerView.setLayoutManager(glm);
        }
        // false because the text in the bottom part of the card may vary its length
        mRecyclerView.setHasFixedSize(false);

        mRecyclerView.setAdapter(mFavoritesAdapter);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(EXTRA_FAVORITE_POSITION)) {
            int position = intent.getIntExtra(EXTRA_FAVORITE_POSITION, -1);
            if (position >= 0 && position < mFavoritesAdapter.getItemCount()) {
                mRecyclerView.scrollToPosition(position);
            }
        }

        FavoritesListViewModel favoritesListViewModel = ViewModelProviders.of(this).get(FavoritesListViewModel.class);
        favoritesListViewModel.getModelList().observe(this,
            motivations -> {
                if (motivations == null || motivations.isEmpty()) {
                    mRecyclerView.setVisibility(View.GONE);
                    mTVErrorMessage.setVisibility(View.VISIBLE);
                    mTVErrorMessage.setText(R.string.no_favorites);
                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mTVErrorMessage.setVisibility(View.GONE);
                }
                mFavoritesAdapter.setMotivations(motivations);
            }
        );

        return view;
    }

    public void setSubmissionCardListener(SubmissionCardListener listener) {
        mSubmissionListener = listener;
    }
    public void setAnalyticsListener(NeverTooLateUtil.AnalyticsListener analyticsListener) {
        mAnalyticsListener = analyticsListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSubmissionListener = null;
    }
}
