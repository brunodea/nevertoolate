package br.brunodea.nevertoolate.frag;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.model.ListingSubmissionParcelable;

public class SubmissionRecyclerViewAdapter extends RecyclerView.Adapter<SubmissionCardViewHolder> {
    private ListingSubmissionParcelable mRedditPosts;
    private final SubmissionCardListener mSubmissionCardListener;
    private boolean mIsFavoritesScreen;

    SubmissionRecyclerViewAdapter(SubmissionCardListener listener, boolean is_favorites_screen) {
        mRedditPosts = null;
        mSubmissionCardListener = listener;
        mIsFavoritesScreen = is_favorites_screen;
    }

    ListingSubmissionParcelable getRedditPosts() {
        return mRedditPosts;
    }

    void setRedditPosts(ListingSubmissionParcelable submissions) {
        mRedditPosts = submissions;
        notifyDataSetChanged();
    }

    @Override
    public SubmissionCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.submission_item, parent, false);
        return new SubmissionCardViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(final SubmissionCardViewHolder holder, int position) {
        holder.onBind(mRedditPosts.at(position), mSubmissionCardListener, mIsFavoritesScreen);
    }

    @Override
    public int getItemCount() {
        return mRedditPosts == null ? 0 : mRedditPosts.size();
    }
}
