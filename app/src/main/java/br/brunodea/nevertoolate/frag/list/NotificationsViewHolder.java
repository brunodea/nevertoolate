package br.brunodea.nevertoolate.frag.list;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.act.FullscreenImageActivity;
import br.brunodea.nevertoolate.model.NotificationModel;
import br.brunodea.nevertoolate.model.SubmissionParcelable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.iv_notification_type) ImageView mIVNotificationType;
    @BindView(R.id.tv_notification_title) TextView mTVTitle;
    @BindView(R.id.cl_notification_layout) ConstraintLayout mCLRoot;

    private Context mContext;

    NotificationsViewHolder(Context context, View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mContext = context;
    }

    void onBind(NotificationModel notificationModel) {
        if (notificationModel.type() == NotificationModel.Type.Time) {
            mIVNotificationType.setImageResource(R.drawable.ic_clock_32dp);
        }
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
                Snackbar.make(mCLRoot, R.string.notification_never_triggered, Snackbar.LENGTH_SHORT)
                        .show();
            }
        });
    }

}
