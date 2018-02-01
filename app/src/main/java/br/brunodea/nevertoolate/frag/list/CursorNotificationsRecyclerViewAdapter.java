package br.brunodea.nevertoolate.frag.list;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.brunodea.nevertoolate.R;
import br.brunodea.nevertoolate.db.NeverTooLateDB;
import br.brunodea.nevertoolate.model.NotificationModel;

public class CursorNotificationsRecyclerViewAdapter extends CursorRecyclerViewAdapter<NotificationsViewHolder> {
    public CursorNotificationsRecyclerViewAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public NotificationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new NotificationsViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(NotificationsViewHolder viewHolder, Cursor cursor) {
        NotificationModel notificationModel = NeverTooLateDB.fromNotificationsTableCursor(mContext, cursor);
        if (notificationModel != null) {
            viewHolder.onBind(notificationModel);
        }
    }
}
