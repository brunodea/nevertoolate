package br.brunodea.nevertoolate.frag.list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.model.ListingSubmissionParcelable;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;

public class SubmissionRecyclerViewAdapter extends RecyclerView.Adapter<SubmissionCardViewHolder> {
    private ListingSubmissionParcelable mRedditPosts;
    private SubmissionCardListener mSubmissionCardListener;
    private NeverTooLateUtil.AnalyticsListener mAnalyticsListener;

    private int mImageFixedSize;

    public SubmissionRecyclerViewAdapter(SubmissionCardListener listener,
                                         NeverTooLateUtil.AnalyticsListener analyticsListener) {
        mRedditPosts = null;
        mSubmissionCardListener = listener;
        mImageFixedSize = 0;
        mAnalyticsListener = analyticsListener;
    }

    public void setFixedImageSize(int image_size) {
        mImageFixedSize = image_size;
    }
    public ListingSubmissionParcelable getRedditPosts() {
        return mRedditPosts;
    }

    public void setRedditPosts(ListingSubmissionParcelable submissions) {
        mRedditPosts = submissions;
        notifyDataSetChanged();
    }

    @Override
    public SubmissionCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.submission_item, parent, false);
        return new SubmissionCardViewHolder(view, parent.getContext(), mAnalyticsListener);
    }

    @Override
    public void onBindViewHolder(final SubmissionCardViewHolder holder, int position) {
        holder.onBind(mRedditPosts.at(position), mSubmissionCardListener, mImageFixedSize);
    }

    @Override
    public int getItemCount() {
        return mRedditPosts == null ? 0 : mRedditPosts.size();
    }
}
