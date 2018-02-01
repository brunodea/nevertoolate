package br.brunodea.nevertoolate.frag.list;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.dean.jraw.models.Submission;

import java.util.Random;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.act.FullscreenImageActivity;
import br.brunodea.nevertoolate.db.NeverTooLateDB;
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
        mCLRoot.setOnClickListener(v -> {
            SubmissionParcelable submission = notificationModel.submission();
            if (submission != null) {
                Intent intent = new Intent(mContext, FullscreenImageActivity.class);
                intent.putExtra(FullscreenImageActivity.ARG_SUBMISSION, submission);
                // TODO: if the item entry has a thumbnail, make an animated transition
                // from it to the fullscreen image.
                mContext.startActivity(intent);
            } else {
                // TODO: remove line below, it is just for testing!!!!
                sendNotification(notificationModel);
                ///////////
                Snackbar.make(mCLRoot, R.string.notification_never_triggered, Snackbar.LENGTH_SHORT)
                        .show();
            }
        });
    }

    // TODO use this method at the correct time
    private void sendNotification(NotificationModel notificationModel) {
        // first, we get a random submission from the top 10 reddit posts from the current day;
        // then, we add the image the submission to the database as "for_notification"
        // then, we update the submission id for the notification model entry in the database;
        // then we actually send the notification
        RedditUtils.queryGetMotivated(submissions -> {
            // since we are going to replace the submission associated with the notification,
            // we should remove the old submission.
            if (notificationModel.submission() != null) {
                NeverTooLateDB.deleteSubmission(mContext, notificationModel.submission(), true);
            }
            Submission s = submissions.get(mRandomGenerator.nextInt(submissions.size()));
            notificationModel.setSubmission(new SubmissionParcelable(s));
            long id = NeverTooLateDB.insertSubmission(mContext, notificationModel.submission(), true);
            notificationModel.setSubmissionId(id);
            NeverTooLateDB.updateNotificationSubmissionId(mContext, notificationModel);
            NotificationUtil.notifyAboutRedditSubmission(mContext, notificationModel);
        }, 10);
    }
}
