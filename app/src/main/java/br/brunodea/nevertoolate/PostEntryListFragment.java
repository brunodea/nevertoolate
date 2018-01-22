package br.brunodea.nevertoolate;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PostEntryListFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostEntryListFragment() {
    }

    public static PostEntryListFragment newInstance(int columnCount) {
        PostEntryListFragment fragment = new PostEntryListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_postentry_list, container, false);
        final RecyclerView recyclerView = view.findViewById(R.id.rv_posts);

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), mColumnCount));
        }
        new ReauthenticationTask(redditClient -> {
            DefaultPaginator<Submission> getMotivated = redditClient
                    .subreddit("GetMotivated")
                    .posts()
                    .sorting(SubredditSort.HOT)
                    .timePeriod(TimePeriod.DAY)
                    .limit(20)
                    .build();
            Listing<Submission> posts = getMotivated.next();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                posts.removeIf(p -> !p.getTitle().toLowerCase().contains("[image]"));
            } else {
                Listing<Submission> aux = posts;
                for (Submission s : posts) {
                    if (!s.getTitle().toLowerCase().contains("[image]")) {
                        aux.remove(s);
                    }
                }
                posts = aux;
            }
            recyclerView.setAdapter(new MyPostEntryRecyclerViewAdapter(getContext(), posts, mListener));

        }).execute();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnListFragmentInteractionListener {
        void onActionFavorite(Submission submission);
        void onActionShare(Submission submission);
        void onActionReddit(Submission submission);
    }

    private static class ReauthenticationTask extends AsyncTask<Void, Void, RedditClient> {
        private RedditLoadingListener mListener;

        public ReauthenticationTask(RedditLoadingListener listener) {
            mListener = listener;
        }

        @Override
        protected RedditClient doInBackground(Void... voids) {
            return NeverTooLateApp.getAccountHelper().switchToUserless();
        }
        @Override
        protected void onPostExecute(RedditClient redditClient) {
            if (mListener != null) {
                mListener.finishedLoading(redditClient);
            }
        }

        public interface RedditLoadingListener {
            void finishedLoading(RedditClient redditClient);
        }
    }
}
