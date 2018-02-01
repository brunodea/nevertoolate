package br.brunodea.nevertoolate.frag.list;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;

import java.util.Random;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.model.NotificationModel;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import br.brunodea.nevertoolate.util.NotificationUtil;
import br.brunodea.nevertoolate.util.RedditUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_notification_title) TextView mTVTitle;
    @BindView(R.id.cl_notification_layout) ConstraintLayout mCLRoot;

    private Context mContext;
    private Random mRandomGenerator;

    NotificationsViewHolder(Context context, View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mContext = context;
        mRandomGenerator = new Random();
    }

    void onBind(NotificationModel notificationModel) {
        mTVTitle.setText(notificationModel.info());
        mCLRoot.setOnClickListener(v -> RedditUtils.queryGetMotivated(submissions -> {
            // TODO: only change the NotificationModel's Submission at the correct time.
            // TODO: fix the database - update the notification's submission id
            Submission s = submissions.get(mRandomGenerator.nextInt(submissions.size()));
            notificationModel.setSubmission(new SubmissionParcelable(s));
            NotificationUtil.notifyAboutRedditSubmission(mContext, notificationModel);
        }, 10));
    }
}
