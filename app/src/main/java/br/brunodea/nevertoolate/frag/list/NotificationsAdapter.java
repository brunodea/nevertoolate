package br.brunodea.nevertoolate.frag.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.entity.Notification;
import br.brunodea.nevertoolate.model.NotificationModel;
import br.brunodea.nevertoolate.util.NeverTooLateDBUtil;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsViewHolder> {

    private NeverTooLateDatabase mDB;
    private List<Notification> mNotifications;

    public NotificationsAdapter(List<Notification> notifications,
                                NeverTooLateDatabase db) {
        mDB = db;
        mNotifications = notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        mNotifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new NotificationsViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position) {
        Notification notification = mNotifications.get(position);
        NotificationModel.Type type = NotificationModel.Type.Invalid;
        switch (notification.type) {
            case TIME:
                type = NotificationModel.Type.Time;
                break;
            case GEOFENCE:
                type = NotificationModel.Type.GeoFence;
                break;
        }

        NotificationModel model = new NotificationModel(notification.info, type.ordinal(),
                notification.id,
                notification.motivationId,
                NeverTooLateDBUtil.from(mDB, mDB.getMotivationDao().findbyId(notification.motivationId)));

        holder.onBind(model);
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }
}
