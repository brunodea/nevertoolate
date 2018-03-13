package br.brunodea.nevertoolate.frag.list;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.dean.jraw.models.Submission;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.act.FullscreenImageActivity;
import br.brunodea.nevertoolate.db.entity.Notification;
import br.brunodea.nevertoolate.db.join.NotificationMotivationRedditImageJoin;
import br.brunodea.nevertoolate.util.RedditUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.iv_notification_type) ImageView mIVNotificationType;
    @BindView(R.id.tv_notification_title) TextView mTVTitle;
    @BindView(R.id.tv_notification_subtitle) TextView mTVSubtitle;
    @BindView(R.id.cl_notification_layout) ConstraintLayout mCLRoot;

    private Context mContext;
    private Notification mNotification;

    NotificationsViewHolder(Context context, View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mContext = context;
        mNotification = null;
    }

    void onBind(NotificationMotivationRedditImageJoin notification) {
        mNotification = notification.notification;
        switch (mNotification.type) {
            case TIME:
                mIVNotificationType.setImageResource(R.drawable.ic_clock_32dp);
                break;
            case GEOFENCE:
                mIVNotificationType.setImageResource(R.drawable.ic_geofence_32dp);
                break;
        }
        mTVTitle.setText(mNotification.info);
        mCLRoot.setOnClickListener(v -> {
            Submission submission = null;
            if (notification.motivation_reddit_image != null) {
                submission = RedditUtils.fromString(notification.motivation_reddit_image.submission_json);
            }
            if (submission != null) {
                Intent intent = new Intent(mContext, FullscreenImageActivity.class);
                intent.putExtra(FullscreenImageActivity.ARG_SUBMISSION, RedditUtils.toString(submission));
                mContext.startActivity(intent);
            } else {
                Snackbar.make(mCLRoot, R.string.notification_never_triggered, Snackbar.LENGTH_SHORT)
                        .show();
            }
        });

        if (notification.motivation_reddit_image == null) {
            mTVSubtitle.setVisibility(View.GONE);
        }
    }

    public Notification notification() {
        return mNotification;
    }
}
