package br.brunodea.nevertoolate.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

import br.brunodea.nevertoolate.db.dao.MotivationRedditImageDaoAsyncTask;
import br.brunodea.nevertoolate.db.dao.NotificationDaoAsyncTask;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;
import br.brunodea.nevertoolate.db.entity.Notification;
import br.brunodea.nevertoolate.db.entity.NotificationTypeConverter;
import br.brunodea.nevertoolate.util.RedditUtils;

// Keep this class just for compatibility reasons. That is,
// just to be able to migrate from the old database style to Room.
public class NeverTooLateDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "NeverTooLateDB";
    private static final int DB_VERSION = 2; // old DB style was version 1.

    private NeverTooLateDatabase mNewDB;

    public NeverTooLateDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        mNewDB = NeverTooLateDatabase.getInstance(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Favorites.SQL_CREATE);
        db.execSQL(Notifications.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                if (newVersion == 2) {
                    Cursor cursor_notification = db.query("notifications",
                            new String[] {"type", "info", "submission_id"},
                            null, null, null, null, null);
                    ArrayList<Notification> notifications = new ArrayList<>();
                    while (cursor_notification.moveToNext()) {
                        Notification.NotificationType type =
                                NotificationTypeConverter.toNotificationType(cursor_notification.getInt(0));
                        String info = cursor_notification.getString(1);
                        long submission_id = cursor_notification.getLong(2);
                        notifications.add(new Notification(type, info, submission_id));
                    }
                    cursor_notification.close();

                    Cursor cursor_motivation = db.query("favorites",
                            new String[] {"url", "permalink", "title", "reddit_id", "for_notification", "_id"},
                            null, null, null, null, null);
                    List<Pair<Motivation, MotivationRedditImage>> motivation_list = new ArrayList<>();
                    long motivation_id = 1; // hopefully this will make the notifications point to the correct motivation.
                    while (cursor_motivation.moveToNext()) {
                        String url = RedditUtils.handleRedditURL(cursor_motivation.getString(0));
                        String permalink = cursor_motivation.getString(1);
                        String title = RedditUtils.handleRedditTitle(cursor_motivation.getString(2));
                        String reddit_id = cursor_motivation.getString(3);
                        boolean for_notification = cursor_motivation.getInt(4) == 1;
                        long _id = cursor_motivation.getLong(5);
                        int notification_index = 0;
                        for (Notification n : notifications) {
                            if (n.base_motivation_id == _id) {
                                // update the motivation notification_id of the notification
                                // by creating a new one with the correct value.
                                notifications.remove(notification_index);
                                notifications.add(new Notification(n.type, n.info, motivation_id));
                                break;
                            }
                            notification_index += 1;
                        }

                        // TODO: check how is the submission json and then create one here
                        MotivationRedditImage motivation_reddit_image = new MotivationRedditImage(
                                permalink, url, reddit_id, title, "", 0);
                        // we can't know if the motivation is for notification but also is favorite
                        // so we make it all favorites, it is easier for the user to simply remove it
                        // from the favorites than losing a favorite. So, we make all motivations
                        // favorite.
                        Motivation motivation = new Motivation(Motivation.MotivationType.REDDIT_IMAGE,
                                0, true);
                        motivation_list.add(Pair.create(motivation, motivation_reddit_image));
                        motivation_id += 1;
                    }
                    cursor_motivation.close();

                    new MotivationRedditImageDaoAsyncTask(motivation_list, mNewDB, MotivationRedditImageDaoAsyncTask.Action.INSERT)
                            .execute();
                    new NotificationDaoAsyncTask(notifications, mNewDB, NotificationDaoAsyncTask.Action.INSERT)
                            .execute();
                }
                break;
            default:
                db.execSQL("DROP TABLE IF EXISTS " + Favorites.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + Notifications.TABLE_NAME);
                onCreate(db);
                break;
        }
    }

    public static final class Favorites {
        static final String TABLE_NAME = "favorites";

        static final String _ID = "_id";
        static final String URL = "url";
        static final String PERMALINK = "permalink";
        static final String TITLE = "title";
        static final String REDDIT_ID = "reddit_id";
        public static final String FOR_NOTIFICATION = "for_notification";
        public static final String[] PROJECTION_ALL =
                {_ID, URL, PERMALINK, TITLE, REDDIT_ID, FOR_NOTIFICATION};
        static final String SQL_CREATE =
                "CREATE TABLE " + TABLE_NAME +
                        " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        URL + " TEXT NOT NULL," +
                        PERMALINK + " TEXT NOT NULL," +
                        TITLE + " TEXT NOT NULL," +
                        REDDIT_ID + " TEXT NOT NULL," +
                        FOR_NOTIFICATION + " INTEGER NOT NULL" + // 0: false
                        ");";
    }

    public static final class Notifications {
        static final String TABLE_NAME = "notifications";

        static final String _ID = "_id";
        public static final String TYPE = "type"; //0 - by time; 1 - by geofence.
        static final String INFO = "info"; // information to be displayed related to the notification
        static final String SUBMISSION_ID = "submission_id";
        public static final String[] PROJECTION_ALL =
                {_ID, TYPE, INFO, SUBMISSION_ID};
        static final String SQL_CREATE =
                "CREATE TABLE " + TABLE_NAME +
                        " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        TYPE + " INTEGER NOT NULL," +
                        INFO + " TEXT NOT NULL," +
                        SUBMISSION_ID + " INTEGER NOT NULL" + //_id of submission in submission table
                        ");";
    }
}
