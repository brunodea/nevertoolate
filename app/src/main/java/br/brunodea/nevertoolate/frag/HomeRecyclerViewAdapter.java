package br.brunodea.nevertoolate.frag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
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

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.cachapa.expandablelayout.ExpandableLayout;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.model.ListingSubmissionParcelable;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {
    private static String TAG = "HomeRecyclerViewAdapter";

    private Context mContext;
    private ListingSubmissionParcelable mRedditPosts;
    private final HomeFragment.OnHomeFragmentListener mHomeFragmentListener;
    private OnPostImageClickListener mOnPostImageClickListener;

    public HomeRecyclerViewAdapter(Context context,
                                   HomeFragment.OnHomeFragmentListener listener,
                                   OnPostImageClickListener postImageClickListener) {
        mContext = context;
        mRedditPosts = null;
        mHomeFragmentListener = listener;
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
        description = description.substring(0, 1).toUpperCase() + description.substring(1);
        holder.mTVDescription.setText(description);

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
        holder.mIVActionFavorite.setOnClickListener(view -> {
            if (mHomeFragmentListener != null) {
                mHomeFragmentListener.onActionFavorite(holder.mRedditPost);
            }
        });
        holder.mIVActionReddit.setOnClickListener(view ->{
            if (mHomeFragmentListener != null) {
                mHomeFragmentListener.onActionReddit(holder.mRedditPost);
            }
        });
        holder.mIVActionShare.setOnClickListener(view -> {
            if (mHomeFragmentListener != null) {
                mHomeFragmentListener.onActionShare(holder.mRedditPost);
            }
        });

        holder.mIVPostImage.setOnClickListener(view1 -> {
            if (mOnPostImageClickListener != null) {
                mOnPostImageClickListener.onClick(holder.mIVPostImage);
            }
        });

        String url = holder.mRedditPost.url();
        if (url.contains("imgur")) {
            // If the link is for imgur, we need to change it to the address of the image location itself.
            // By appending a lowercase L to the imgur's image hash, we get a smaller image
            if (url.contains("/imgur")) {
                url = url.replace("/imgur", "/i.imgur");
                url += "l.jpg";
            } else {
                String ext = url.substring(url.lastIndexOf("."), url.length());
                String x_ext = "l" + ext;
                url = url.replace(ext, x_ext);
            }
        }

        Log.i(TAG, url);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setSize(holder.mIVPostImage.getWidth(), 200);
        gradientDrawable.setColor(mContext.getResources().getColor(android.R.color.white));

        holder.mIVPostImage.setImageDrawable(gradientDrawable);
        Picasso.with(mContext)
                .load(url)
                .placeholder(gradientDrawable)
                .into(holder.mIVPostImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.mPBLoadingImage.setVisibility(View.GONE);
                        holder.mIVPostImage.setVisibility(View.VISIBLE);
                        holder.mImageErrorLayout.setVisibility(View.GONE);

                        Bitmap bitmap = ((BitmapDrawable)holder.mIVPostImage.getDrawable()).getBitmap();
                        final double viewWidthToBitmapWidthRatio = (double)holder.mIVPostImage.getWidth() / (double)bitmap.getWidth();
                        ViewGroup.LayoutParams params = holder.mIVPostImage.getLayoutParams();
                        params.height = (int) (bitmap.getHeight() * viewWidthToBitmapWidthRatio);
                        holder.mIVPostImage.setLayoutParams(params);
                    }

                    @Override
                    public void onError() {
                        holder.mPBLoadingImage.setVisibility(View.GONE);
                        holder.mIVPostImage.setVisibility(View.GONE);
                        holder.mImageErrorLayout.setVisibility(View.VISIBLE);
                    }
                });
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
    }

    public interface OnPostImageClickListener {
        void onClick(ImageView imageView);
    }
}
