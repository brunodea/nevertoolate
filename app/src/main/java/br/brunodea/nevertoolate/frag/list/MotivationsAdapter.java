package br.brunodea.nevertoolate.frag.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.join.MotivationRedditImageJoin;
import br.brunodea.nevertoolate.util.NeverTooLateDBUtil;
import br.brunodea.nevertoolate.util.NeverTooLateUtil;
import br.brunodea.nevertoolate.util.RedditUtils;

public class MotivationsAdapter extends RecyclerView.Adapter<SubmissionCardViewHolder> {

    private SubmissionCardListener mSubmissionCardListener;
    private int mImageFixedSize;
    private NeverTooLateUtil.AnalyticsListener mAnalyticsListener;
    private List<MotivationRedditImageJoin> mMotivations;
    private NeverTooLateDatabase mDB;

    public MotivationsAdapter(List<MotivationRedditImageJoin> motivations,
                              NeverTooLateDatabase db,
                              SubmissionCardListener submissionCardListener,
                              NeverTooLateUtil.AnalyticsListener analyticsListener) {
        mDB = db;
        mMotivations = motivations;
        mSubmissionCardListener = submissionCardListener;
        mAnalyticsListener = analyticsListener;
        mImageFixedSize = 0;
    }

    public void setFixedImageSize(int image_size) {
        mImageFixedSize = image_size;
    }

    public void setMotivations(List<MotivationRedditImageJoin> motivations) {
        mMotivations = motivations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubmissionCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.submission_item, parent, false);
        return new SubmissionCardViewHolder(view, parent.getContext(), mAnalyticsListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionCardViewHolder holder, int position) {
        MotivationRedditImageJoin motivation = mMotivations.get(position);
        holder.onBind(RedditUtils.fromString(motivation.motivation_reddit_image.submission_json),
                mSubmissionCardListener, mImageFixedSize);
    }

    @Override
    public int getItemCount() {
        return mMotivations == null ? 0 : mMotivations.size();
    }
}
