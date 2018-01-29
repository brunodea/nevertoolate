package br.brunodea.nevertoolate.frag;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import br.brunodea.nevertoolate.model.ListingSubmissionParcelable;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.util.GlideApp;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {
    private static String TAG = "HomeRecyclerViewAdapter";

    private Context mContext;
    private ListingSubmissionParcelable mRedditPosts;
    private final SubmissionCardListener mSubmissionCardListener;
    private OnPostImageClickListener mOnPostImageClickListener;

    public HomeRecyclerViewAdapter(Context context,
                                   SubmissionCardListener listener,
                                   OnPostImageClickListener postImageClickListener) {
        mContext = context;
        mRedditPosts = null;
        mSubmissionCardListener = listener;
        mOnPostImageClickListener = postImageClickListener;
    }

    public ListingSubmissionParcelable getRedditPosts() {
        return mRedditPosts;
    }
    public void setRedditPosts(ListingSubmissionParcelable submissions) {
        mRedditPosts = submissions;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.submission_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mRedditPost = mRedditPosts.at(position);

        // Remove the tag from the title and capitalize its first letter.
        String description = holder.mRedditPost.title();
        description = description.replace(
               description.substring(0, description.indexOf("]") + 1),
                ""
        ).trim();
        if (description.length() > 1) {
            description = description.substring(0, 1).toUpperCase() + description.substring(1);
            holder.mTVDescription.setText(description);
        }

        // Expand/Collaspe the image title based on a tag added to the image view.
        holder.mIVActionExpand.setOnClickListener(view -> {
            String tag_down = mContext.getString(R.string.card_action_expand_tag_down);
            String tag_up = mContext.getString(R.string.card_action_expand_tag_up);
            if (holder.mIVActionExpand.getTag().equals(tag_down)) {
                holder.mIVActionExpand.setImageResource(R.drawable.ic_expand_up_24dp);
                holder.mIVActionExpand.setTag(tag_up);
                holder.mExpandableLayout.expand();
            } else {
                holder.mIVActionExpand.setImageResource(R.drawable.ic_expand_down_24dp);
                holder.mIVActionExpand.setTag(tag_down);
                holder.mExpandableLayout.collapse();
            }
        });
        holder.adjust_favorite_icon(NeverTooLateDB.isFavorite(mContext, holder.mRedditPost));
        holder.mIVActionFavorite.setOnClickListener(view -> {
            if (mSubmissionCardListener != null) {
                boolean is_favorite = mSubmissionCardListener.onActionFavorite(holder.mRedditPost);
                holder.adjust_favorite_icon(is_favorite);
            }
        });
        holder.mIVActionReddit.setOnClickListener(view ->{
            if (mSubmissionCardListener != null) {
                mSubmissionCardListener.onActionReddit(holder.mRedditPost);
            }
        });
        holder.mIVActionShare.setOnClickListener(view -> {
            if (mSubmissionCardListener != null) {
                mSubmissionCardListener.onActionShare(
                        holder.mRedditPost,
                        NeverTooLateUtil.getLocalBitmapUri(mContext, holder.mIVPostImage)
                );
            }
        });

        holder.mIVPostImage.setOnClickListener(view1 -> {
            if (mOnPostImageClickListener != null) {
                mOnPostImageClickListener.onClick(holder.mIVPostImage, holder.mRedditPost);
            }
        });


        Log.i(TAG, holder.mRedditPost.url());
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setSize(holder.mIVPostImage.getWidth(), 200);
        gradientDrawable.setColor(mContext.getResources().getColor(android.R.color.white));

        GlideApp.with(mContext)
                .load(holder.mRedditPost.url())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.mIVPostImage.setVisibility(View.GONE);
                        holder.mImageErrorLayout.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.mImageErrorLayout.setVisibility(View.GONE);
                        holder.mIVPostImage.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .placeholder(gradientDrawable)
                .into(holder.mIVPostImage);
    }

    @Override
    public int getItemCount() {
        return mRedditPosts == null ? 0 : mRedditPosts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
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

        SubmissionParcelable mRedditPost;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void adjust_favorite_icon(boolean is_favorite) {
            if (is_favorite) {
                mIVActionFavorite.setImageDrawable(mContext.getDrawable(R.drawable.ic_favorite_24dp));
            } else {
                mIVActionFavorite.setImageDrawable(mContext.getDrawable(R.drawable.ic_favorite_outline_24dp));
            }
        }
    }

    public interface OnPostImageClickListener {
        void onClick(ImageView imageView, SubmissionParcelable submission);
    }
}
