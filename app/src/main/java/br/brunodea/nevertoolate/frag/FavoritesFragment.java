
package br.brunodea.nevertoolate.frag;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.act.MainActivity;
import br.brunodea.nevertoolate.db.NeverTooLateContract;
import br.brunodea.nevertoolate.db.NeverTooLateDB;
import br.brunodea.nevertoolate.db.NeverTooLateDBHelper;
import br.brunodea.nevertoolate.model.ListingSubmissionParcelable;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link SubmissionCardListener}
 * interface.
 */
public class FavoritesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 1;

    private CursorSubmissionRecyclerViewAdapter mSubmissionRecylcerViewAdapter;
    private SubmissionCardListener mSubmissionListener;

    @BindView(R.id.rv_posts) RecyclerView mRecyclerView;
    @BindView(R.id.tv_error_message) TextView mTVErrorMessage;
    @BindView(R.id.swiperefresh) SwipeRefreshLayout mSwipeRefreshLayout;

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
        mSubmissionRecylcerViewAdapter = new CursorSubmissionRecyclerViewAdapter(getContext(),
                null, mSubmissionListener);

        setHasOptionsMenu(false);

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
        mRecyclerView.setAdapter(mSubmissionRecylcerViewAdapter);
        getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SubmissionCardListener) {
            mSubmissionListener = (SubmissionCardListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFavoritesFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSubmissionListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case LOADER_ID:
                return new CursorLoader(
                        getContext(),
                        NeverTooLateContract.FAVORITES_CONTENT_URI,
                        NeverTooLateDBHelper.Favorites.PROJECTION_ALL,
                        null, null, null
                );
            default:
                throw new IllegalArgumentException("Illegal loader ID: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_ID:
                mSubmissionRecylcerViewAdapter.changeCursor(cursor);
                if (mSubmissionRecylcerViewAdapter.getItemCount() == 0) {
                    mRecyclerView.setVisibility(View.GONE);
                    mTVErrorMessage.setVisibility(View.VISIBLE);
                    mTVErrorMessage.setText(R.string.no_favorites);
                }
                break;
            default:
                throw new IllegalArgumentException("Illegal loader ID: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
