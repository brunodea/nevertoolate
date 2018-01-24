package br.brunodea.nevertoolate.frag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import br.brunodea.nevertoolate.R;
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
    private Animator mCurrAnimator;

    @BindView(R.id.rv_posts) RecyclerView mRecyclerView;
    @BindView(R.id.iv_post_image_expand) ImageView mIVExpand;
    @BindView(R.id.fl_posts_container) FrameLayout mFLPostsContainer;
    @BindView(R.id.tv_error_message) TextView mTVErrorMessage;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.submission_list, container, false);
        ButterKnife.bind(this, view);

        if (mHomeRecyclerViewAdapter == null) {
            mHomeRecyclerViewAdapter = new HomeRecyclerViewAdapter(
                    getContext(),
                    mFragmentInteractionListener,
                    imageView -> zoomPostImage(mFLPostsContainer, imageView, mIVExpand)
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
                mTVErrorMessage.setVisibility(View.GONE);
                mFragmentInteractionListener.onStartLoadingPosts();

                RedditUtils.queryGetMotivated(submissions -> {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mFragmentInteractionListener.onFinishedLoadingPosts();
                    mHomeRecyclerViewAdapter.setRedditPosts(new ListingSubmissionParcelable(submissions));
                });
            } else {
                mRecyclerView.setVisibility(View.GONE);
                mTVErrorMessage.setVisibility(View.VISIBLE);
            }
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
        void onStartLoadingPosts();
        void onFinishedLoadingPosts();
        void onActionFavorite(SubmissionParcelable submission);
        void onActionShare(SubmissionParcelable submission);
        void onActionReddit(SubmissionParcelable submission);
    }
}
