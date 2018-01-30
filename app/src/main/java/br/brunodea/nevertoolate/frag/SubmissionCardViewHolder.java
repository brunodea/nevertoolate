package br.brunodea.nevertoolate.frag;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.cachapa.expandablelayout.ExpandableLayout;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDB;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.util.GlideApp;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;
import butterknife.BindView;
import butterknife.ButterKnife;

class SubmissionCardViewHolder extends RecyclerView.ViewHolder {
    private static String TAG = "SubmissionCardViewHolder";

    @BindView(R.id.cv_card_post_container) CardView mCardView;
    @BindView(R.id.cl_post_container) ConstraintLayout mCLPostContainer;
    @BindView(R.id.image_error_layout) LinearLayout mImageErrorLayout;
    @BindView(R.id.iv_post_image) ImageView mIVPostImage;
    @BindView(R.id.iv_post_favorite) ImageView mIVActionFavorite;
    @BindView(R.id.iv_post_reddit) ImageView mIVActionReddit;
    @BindView(R.id.iv_post_share) ImageView mIVActionShare;
    @BindView(R.id.iv_post_expand) ImageView mIVActionExpand;
    @BindView(R.id.tv_post_description) TextView mTVDescription;
    @BindView(R.id.pb_loading_image) ProgressBar mPBLoadingImage;
    @BindView(R.id.expandable_layout) ExpandableLayout mExpandableLayout;

    private Context mContext;

    SubmissionCardViewHolder(View view, Context context) {
        super(view);
        ButterKnife.bind(this, view);
        mContext = context;
    }

    void onBind(SubmissionParcelable submission,
                SubmissionCardListener submissionCardListener,
                boolean is_favorites_screen) {
        // Remove the tag from the title and capitalize its first letter.
        String description = submission.title();
        description = description.replace(
                description.substring(0, description.indexOf("]") + 1),
                ""
        ).trim();
        if (description.length() > 1) {
            description = description.substring(0, 1).toUpperCase() + description.substring(1);
            mTVDescription.setText(description);
        }

        // Expand/Collaspe the image title based on a tag added to the image view.
        mIVActionExpand.setOnClickListener(view -> {
            String tag_down = mContext.getString(R.string.card_action_expand_tag_down);
            String tag_up = mContext.getString(R.string.card_action_expand_tag_up);
            if (mIVActionExpand.getTag().equals(tag_down)) {
                mIVActionExpand.setImageResource(R.drawable.ic_expand_up_24dp);
                mIVActionExpand.setTag(tag_up);
                mExpandableLayout.expand();
            } else {
                mIVActionExpand.setImageResource(R.drawable.ic_expand_down_24dp);
                mIVActionExpand.setTag(tag_down);
                mExpandableLayout.collapse();
            }
        });
        adjust_favorite_icon(is_favorites_screen || NeverTooLateDB.isFavorite(mContext, submission));
        mIVActionFavorite.setOnClickListener(view -> {
            if (submissionCardListener != null) {
                boolean is_favorite = submissionCardListener.onActionFavorite(submission);
                adjust_favorite_icon(is_favorite);
            }
        });
        mIVActionReddit.setOnClickListener(view ->{
            if (submissionCardListener != null) {
                submissionCardListener.onActionReddit(submission);
            }
        });
        mIVActionShare.setOnClickListener(view -> {
            if (submissionCardListener != null) {
                submissionCardListener.onActionShare(
                        submission,
                        NeverTooLateUtil.getLocalBitmapUri(mContext, mIVPostImage)
                );
            }
        });

        mIVPostImage.setOnClickListener(view1 -> {
            if (submissionCardListener != null) {
                submissionCardListener.onImageClick(mIVPostImage, submission);
            }
        });


        Log.i(TAG, submission.url());
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setSize(mIVPostImage.getWidth(), 200);
        gradientDrawable.setColor(mContext.getResources().getColor(android.R.color.white));

        GlideApp.with(mContext)
                .load(submission.url())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mIVPostImage.setVisibility(View.GONE);
                        mImageErrorLayout.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mImageErrorLayout.setVisibility(View.GONE);
                        mIVPostImage.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .placeholder(gradientDrawable)
                .into(mIVPostImage);
    }

    private void adjust_favorite_icon(boolean is_favorite) {
        if (is_favorite) {
            mIVActionFavorite.setImageDrawable(mContext.getDrawable(R.drawable.ic_favorite_24dp));
        } else {
            mIVActionFavorite.setImageDrawable(mContext.getDrawable(R.drawable.ic_favorite_outline_24dp));
        }
    }
}
