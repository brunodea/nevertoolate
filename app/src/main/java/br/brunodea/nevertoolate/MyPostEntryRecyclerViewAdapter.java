package br.brunodea.nevertoolate;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;

import br.brunodea.nevertoolate.PostEntryListFragment.OnListFragmentInteractionListener;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MyPostEntryRecyclerViewAdapter extends RecyclerView.Adapter<MyPostEntryRecyclerViewAdapter.ViewHolder> {

    private Context mContext;
    private Listing<Submission> mRedditPosts;
    private final OnListFragmentInteractionListener mListener;

    // TODO: make sure reddit_posts only contain posts with images in the URL
    public MyPostEntryRecyclerViewAdapter(Context context, Listing<Submission> reddit_posts,
                                          OnListFragmentInteractionListener listener) {
        mContext = context;
        mRedditPosts = reddit_posts;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_postentry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mRedditPost = mRedditPosts.get(position);
        Picasso.with(mContext)
                .load(holder.mRedditPost.getUrl())
                .into(holder.mIVPostImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.mIVPostImage.setVisibility(View.VISIBLE);
                        holder.mImageErrorLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        holder.mIVPostImage.setVisibility(View.GONE);
                        holder.mImageErrorLayout.setVisibility(View.VISIBLE);
                    }
                });
        holder.mIVActionExpand.setOnClickListener(view -> {
            TransitionManager.beginDelayedTransition(holder.mCLPostContainer);
            String tag_down = mContext.getString(R.string.card_action_expand_tag_down);
            String tag_up = mContext.getString(R.string.card_action_expand_tag_up);
            if (holder.mIVActionExpand.getTag().equals(tag_down)) {
                holder.mIVActionExpand.setImageResource(R.drawable.ic_expand_up_24dp);
                holder.mIVActionExpand.setTag(tag_up);
                holder.mTVDescription.setVisibility(View.VISIBLE);
            } else {
                holder.mIVActionExpand.setImageResource(R.drawable.ic_expand_down_24dp);
                holder.mIVActionExpand.setTag(tag_down);
                holder.mTVDescription.setVisibility(View.GONE);
            }
        });
        holder.mIVActionFavorite.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onActionFavorite(holder.mRedditPost);
            }
        });
        holder.mIVActionReddit.setOnClickListener(view ->{
            if (mListener != null) {
                mListener.onActionReddit(holder.mRedditPost);
            }
        });
        holder.mIVActionShare.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.onActionShare(holder.mRedditPost);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRedditPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cl_post_container) ConstraintLayout mCLPostContainer;
        @BindView(R.id.image_error_layout) LinearLayout mImageErrorLayout;
        @BindView(R.id.iv_post_image) ImageView mIVPostImage;
        @BindView(R.id.iv_post_favorite) ImageView mIVActionFavorite;
        @BindView(R.id.iv_post_reddit) ImageView mIVActionReddit;
        @BindView(R.id.iv_post_share) ImageView mIVActionShare;
        @BindView(R.id.iv_post_expand) ImageView mIVActionExpand;
        @BindView(R.id.tv_post_description) TextView mTVDescription;

        private Submission mRedditPost;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
