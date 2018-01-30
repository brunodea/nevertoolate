package br.brunodea.nevertoolate.frag.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.model.NotificationModel;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_title) TextView mTVTitle;

    NotificationsViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(itemView);
    }

    void onBind(NotificationModel notificationModel) {
        mTVTitle.setText(notificationModel.info());
    }
}
