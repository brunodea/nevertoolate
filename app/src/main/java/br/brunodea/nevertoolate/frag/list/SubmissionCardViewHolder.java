package br.brunodea.nevertoolate.frag.list;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.dean.jraw.models.Submission;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.util.GlideApp;
import br.brunodea.nevertoolate.util.GlideRequest;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;
import br.brunodea.nevertoolate.util.RedditUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SubmissionCardViewHolder extends RecyclerView.ViewHolder {
    private static String TAG = "SubmissionCardViewHolder";

    @BindView(R.id.cv_card_post_container) CardView mCardView;
    @BindView(R.id.cl_post_container) ConstraintLayout mCLPostContainer;
    @BindView(R.id.image_error_layout) LinearLayout mImageErrorLayout;
    @BindView(R.id.iv_post_image) ImageView mIVPostImage;
    @BindView(R.id.pb_loading_image) ProgressBar mPBLoadingImage;

    private Context mContext;
    private SubmissionActions mSubmissionActions;
    private View mView;

    SubmissionCardViewHolder(View view, Context context, NeverTooLateUtil.AnalyticsListener analyticsListener) {
        super(view);
        ButterKnife.bind(this, view);
        mView = view;
        mContext = context;
        mSubmissionActions = new SubmissionActions(mContext, analyticsListener);
    }

    void onBind(Submission submission,
                SubmissionCardListener submissionCardListener,
                int image_fixed_size) {
        mSubmissionActions.onBind(mView, submission,
                submissionCardListener, mIVPostImage, image_fixed_size > 0);
        mIVPostImage.setOnClickListener(view1 -> {
            if (submissionCardListener != null) {
                submissionCardListener.onImageClick(mIVPostImage, submission);
            }
        });
        mIVPostImage.getLayoutParams().height =
                mContext.getResources().getDimensionPixelOffset(R.dimen.image_default_size);

        Log.i(TAG, submission.getUrl());
        mPBLoadingImage.setVisibility(View.VISIBLE);
        GlideRequest<Drawable> req = GlideApp.with(mContext)
                .load(RedditUtils.handleRedditURL(submission.getUrl()))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mIVPostImage.setVisibility(View.GONE);
                        mPBLoadingImage.setVisibility(View.GONE);
                        mSubmissionActions.disableShare();
                        mImageErrorLayout.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mImageErrorLayout.setVisibility(View.GONE);
                        mPBLoadingImage.setVisibility(View.GONE);
                        mIVPostImage.setVisibility(View.VISIBLE);
                        mIVPostImage.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        return false;
                    }
                });
        if (image_fixed_size > 0) {
            mIVPostImage.getLayoutParams().height = image_fixed_size;
            req = req.centerCrop();
        }
        req.into(mIVPostImage);
    }
}
