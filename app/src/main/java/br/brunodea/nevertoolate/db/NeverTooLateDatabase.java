package br.brunodea.nevertoolate.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import br.brunodea.nevertoolate.db.dao.DaoAsyncTask;
import br.brunodea.nevertoolate.db.dao.EntityDao;
import br.brunodea.nevertoolate.db.dao.MotivationDao;
import br.brunodea.nevertoolate.db.dao.MotivationRedditImageDao;
import br.brunodea.nevertoolate.db.dao.NotificationDao;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;
import br.brunodea.nevertoolate.db.entity.Notification;

@Database(entities = {Notification.class, Motivation.class, MotivationRedditImage.class},
          version = 2)
public abstract class NeverTooLateDatabase extends RoomDatabase {
    private static final String DB_NAME = "nevertoolate.db";
    private static volatile NeverTooLateDatabase sInstance;

    public static synchronized NeverTooLateDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = create(context);
        }
        return sInstance;
    }

    private static NeverTooLateDatabase create(final Context context) {
        return Room.databaseBuilder(context,
                NeverTooLateDatabase.class,
                DB_NAME).build();
    }

    public abstract NotificationDao getNotificationDao();
    public abstract MotivationDao getMotivationDao();
    public abstract MotivationRedditImageDao getMotivationRedditImageDao();
}
