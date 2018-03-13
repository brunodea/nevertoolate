package br.brunodea.nevertoolate.frag.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.join.NotificationMotivationRedditImageJoin;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsViewHolder> {

    private NeverTooLateDatabase mDB;
    private List<NotificationMotivationRedditImageJoin> mNotifications;

    public NotificationsAdapter(List<NotificationMotivationRedditImageJoin> notifications,
                                NeverTooLateDatabase db) {
        mDB = db;
        mNotifications = notifications;
    }

    public void setNotifications(List<NotificationMotivationRedditImageJoin> notifications) {
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
        holder.onBind(mNotifications.get(position));
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }
}
