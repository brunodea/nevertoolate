package br.brunodea.nevertoolate.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NeverTooLateDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "NeverTooLateDB";
    private static final int DB_VERSION = 2;


    NeverTooLateDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
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
                    Cursor c = db.query("favorites",
                            new String[] {"url", "permalink", "title", "reddit_id", "for_notification"},
                            null, null, null, null, null);
                }
                break;
            default:
                db.execSQL("DROP TABLE IF EXISTS " + Favorites.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + Notifications.TABLE_NAME);
                break;
        }
        onCreate(db);
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
