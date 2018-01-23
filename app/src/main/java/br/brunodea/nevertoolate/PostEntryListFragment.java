package br.brunodea.nevertoolate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;

import java.util.Iterator;

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
    private Animator mCurrAnimator;

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
        final ImageView expandedImageView = view.findViewById(R.id.iv_post_image_expand);
        final FrameLayout postsContainer = view.findViewById(R.id.fl_posts_container);
        TextView tvNoInternet = view.findViewById(R.id.tv_no_internet);
        if (NTLUtil.isOnline(getContext())) {
            tvNoInternet.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), mColumnCount));
            }
            mListener.onStartLoadingPosts();
            new ReauthenticationTask(posts -> {
                mListener.onFinishedLoadingPosts();
                recyclerView.setAdapter(new MyPostEntryRecyclerViewAdapter(getContext(), posts, mListener,
                        imageView -> zoomPostImage(postsContainer, imageView, expandedImageView)));
            }).execute();
        } else {
            recyclerView.setVisibility(View.GONE);
            tvNoInternet.setVisibility(View.VISIBLE);
        }
        return view;
    }

    private void zoomPostImage(View container, ImageView postImageView, ImageView expandedPostImageView) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrAnimator != null) {
            mCurrAnimator.cancel();
        }
        expandedPostImageView.setImageDrawable(postImageView.getDrawable());

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        postImageView.getGlobalVisibleRect(startBounds);
        container.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        postImageView.setAlpha(0f);
        expandedPostImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedPostImageView.setPivotX(0f);
        expandedPostImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedPostImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedPostImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedPostImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedPostImageView,
                View.SCALE_Y, startScale, 1f));
        int anim_duration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        set.setDuration(anim_duration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrAnimator = null;
            }
        });
        set.start();
        mCurrAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedPostImageView.setOnClickListener(view -> {
            if (mCurrAnimator != null) {
                mCurrAnimator.cancel();
            }

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            AnimatorSet set1 = new AnimatorSet();
            set1.play(ObjectAnimator
                    .ofFloat(expandedPostImageView, View.X, startBounds.left))
                    .with(ObjectAnimator
                            .ofFloat(expandedPostImageView,
                                    View.Y,startBounds.top))
                    .with(ObjectAnimator
                            .ofFloat(expandedPostImageView,
                                    View.SCALE_X, startScaleFinal))
                    .with(ObjectAnimator
                            .ofFloat(expandedPostImageView,
                                    View.SCALE_Y, startScaleFinal));
            set1.setDuration(anim_duration);
            set1.setInterpolator(new DecelerateInterpolator());
            set1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    postImageView.setAlpha(1f);
                    expandedPostImageView.setVisibility(View.GONE);
                    mCurrAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    postImageView.setAlpha(1f);
                    expandedPostImageView.setVisibility(View.GONE);
                    mCurrAnimator = null;
                }
            });
            set1.start();
            mCurrAnimator = set1;
        });
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
        void onStartLoadingPosts();
        void onFinishedLoadingPosts();
        void onActionFavorite(Submission submission);
        void onActionShare(Submission submission);
        void onActionReddit(Submission submission);
    }

    private static class ReauthenticationTask extends AsyncTask<Void, Void, Listing<Submission>> {
        private RedditLoadingListener mRedditLoadingListener;

        public ReauthenticationTask(RedditLoadingListener listener) {
            mRedditLoadingListener = listener;
        }

        @Override
        protected Listing<Submission> doInBackground(Void... voids) {
            DefaultPaginator<Submission> getMotivated = NeverTooLateApp.redditClient()
                    .subreddit("GetMotivated")
                    .posts()
                    .sorting(SubredditSort.HOT)
                    .timePeriod(TimePeriod.DAY)
                    .limit(20)
                    .build();
            Listing<Submission> posts = getMotivated.next();
            // only use posts with the tag "[image]" and do not use imgur albums
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                posts.removeIf(p -> !p.getTitle().toLowerCase().contains("[image]"));
                posts.removeIf(p -> p.getUrl().contains("imgur.com/a/"));
            } else {
                for (Iterator<Submission> it = posts.iterator(); it.hasNext();) {
                    Submission s = it.next();
                    if (!s.getTitle().toLowerCase().contains("[image]") ||
                            s.getUrl().contains("imgur.com/a/")) {
                        it.remove();
                    }
                }
            }
            return posts;
        }
        @Override
        protected void onPostExecute(Listing<Submission> submissions) {
            if (mRedditLoadingListener != null) {
                mRedditLoadingListener.finishedLoading(submissions);
            }
        }

        public interface RedditLoadingListener {
            void finishedLoading(Listing<Submission> submissions);
        }
    }
}
