package br.brunodea.nevertoolate.db.dao;

import android.os.AsyncTask;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;
import br.brunodea.nevertoolate.db.entity.Notification;

public class NotificationDaoAsyncTask extends AsyncTask<Void, Void, Void> {
    public enum Action {
        INSERT,
        DELETE,
        UPDATE
    }

    private Action mAction;
    private NeverTooLateDatabase mDB;
    private List<Notification> mNotifications;

    public interface InsertListener {
        void onInsert(final long new_id);
    }

    private InsertListener mInsertListener;

    public NotificationDaoAsyncTask(Notification notification,
                                    NeverTooLateDatabase db,
                                    Action action) {
        mAction = action;
        mDB = db;
        mNotifications = new ArrayList<>();
        mNotifications.add(notification);
        mInsertListener = null;
    }
    public NotificationDaoAsyncTask(List<Notification> notifications,
                                    NeverTooLateDatabase db,
                                    Action action) {
        mAction = action;
        mDB = db;
        mNotifications = notifications;
        mInsertListener = null;
    }

    public void setInsertListener(final InsertListener listener) {
        mInsertListener = listener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        for (Notification n : mNotifications) {
            switch (mAction) {
                case INSERT:
                    n.id = mDB.getNotificationDao().insert(n);
                    if (mInsertListener != null) {
                        mInsertListener.onInsert(n.id);
                    }
                    break;
                case DELETE:
                    // Case the motivation pointed by this notification is not a favorite, we delete it.
                    Motivation motivation = mDB.getMotivationDao().findbyId(n.motivationId);
                    if (motivation != null && !motivation.favorite) {
                        switch (motivation.type) {
                            case REDDIT_IMAGE:
                                MotivationRedditImage mri = mDB.getMotivationRedditImageDao()
                                        .findById(motivation.motivationId);
                                mDB.getMotivationRedditImageDao().delete(mri);
                                // TODO: make sure we don't need to explicitly delete from the
                                // motivation table.
                                break;
                        }
                    }
                    mDB.getNotificationDao().delete(n);
                    break;
                case UPDATE:
                    mDB.getNotificationDao().update(n);
                    break;
            }
        }
        return null;
    }
}
