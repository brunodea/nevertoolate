package br.brunodea.nevertoolate.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import br.brunodea.nevertoolate.model.NotificationModel;
import br.brunodea.nevertoolate.model.SubmissionParcelable;

public class NeverTooLateDB {
    private static SubmissionParcelable findSubmissionByRedditID(Context context, String reddit_id,
                                                                 boolean for_notification) {
        SubmissionParcelable result = null;
        String selection = NeverTooLateDBHelper.Favorites.REDDIT_ID + " = \"" + reddit_id + "\"";
        if (for_notification) {
            selection += " AND " + NeverTooLateDBHelper.Favorites.FOR_NOTIFICATION + " = 1";
        } else {
            selection += " AND " + NeverTooLateDBHelper.Favorites.FOR_NOTIFICATION + " = 0";
        }
        Cursor c = context.getContentResolver().query(NeverTooLateContract.FAVORITES_CONTENT_URI,
                NeverTooLateDBHelper.Favorites.PROJECTION_ALL,
                selection,
                null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                result = fromFavoritesTableCursor(c);
            }
            c.close();
        }
        return result;
    }
    private static SubmissionParcelable findSubmissionByID(Context context, long id,
                                                                 boolean for_notification) {
        SubmissionParcelable result = null;
        String selection = NeverTooLateDBHelper.Favorites._ID + " = \"" + id + "\"";
        if (for_notification) {
            selection += " AND " + NeverTooLateDBHelper.Favorites.FOR_NOTIFICATION + " = 1";
        } else {
            selection += " AND " + NeverTooLateDBHelper.Favorites.FOR_NOTIFICATION + " = 0";
        }
        Cursor c = context.getContentResolver().query(NeverTooLateContract.FAVORITES_CONTENT_URI,
                NeverTooLateDBHelper.Favorites.PROJECTION_ALL,
                selection,
                null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                result = fromFavoritesTableCursor(c);
            }
            c.close();
        }
        return result;
    }
    public static SubmissionParcelable fromFavoritesTableCursor(Cursor cursor) {
        SubmissionParcelable res = null;
        if (cursor != null) {
            // ignore column 0 (_id).
            String url = cursor.getString(1);
            String permalink = cursor.getString(2);
            String title = cursor.getString(3);
            String reddit_id = cursor.getString(4);

            res = new SubmissionParcelable();
            res.setID(reddit_id);
            res.setURL(url);
            res.setPermalink(permalink);
            res.setTitle(title);
        }

        return res;
    }
    public static boolean isFavorite(Context context, SubmissionParcelable submission) {
        return findSubmissionByRedditID(context, submission.id(), false) != null;
    }

    // returns true if actually inserted a new entry in the database.
    public static boolean insertSubmission(Context context, SubmissionParcelable submission,
                                           boolean for_notification) {
        boolean is_new = findSubmissionByRedditID(context, submission.id(), for_notification) == null;
        // Only add a new submission to the favorites if it is a new one.
        if (is_new) {
            ContentValues cv = new ContentValues();
            cv.put(NeverTooLateDBHelper.Favorites.REDDIT_ID, submission.id());
            cv.put(NeverTooLateDBHelper.Favorites.URL, submission.url());
            cv.put(NeverTooLateDBHelper.Favorites.PERMALINK, submission.permalink());
            cv.put(NeverTooLateDBHelper.Favorites.TITLE, submission.title());
            cv.put(NeverTooLateDBHelper.Favorites.FOR_NOTIFICATION, for_notification ? 1 : 0);

            context.getContentResolver().insert(NeverTooLateContract.FAVORITES_CONTENT_URI, cv);
        }

        return is_new;
    }
    public static void deleteSubmission(Context context, SubmissionParcelable submission) {
        String selection = NeverTooLateDBHelper.Favorites.REDDIT_ID + " = \"" + submission.id() + "\"";
        context.getContentResolver().delete(NeverTooLateContract.FAVORITES_CONTENT_URI, selection, null);
    }

    public static NotificationModel fromNotificationsTableCursor(Context context, Cursor cursor) {
        NotificationModel res = null;
        if (cursor != null) {
            long id = cursor.getLong(0);
            int type = cursor.getInt(1);
            String info = cursor.getString(2);
            long submission_id = cursor.getLong(3);
            res = new NotificationModel(info, type, id, submission_id, findSubmissionByID(context,
                    submission_id, true));
        }

        return res;
    }

    public static void updateNotificationSubmissionId(Context context, NotificationModel nm) {
        ContentValues cv = new ContentValues();
        cv.put(NeverTooLateDBHelper.Notifications.SUBMISSION_ID, nm.submission_id());
        context.getContentResolver().update(NeverTooLateContract.NOTIFICATIONS_CONTENT_URI,
                cv, NeverTooLateDBHelper.Notifications._ID + " = " + nm.id(), null);
    }

    public static void insertNotification(Context context, NotificationModel notificationModel) {
        ContentValues cv = new ContentValues();
        cv.put(NeverTooLateDBHelper.Notifications.INFO, notificationModel.info());
        cv.put(NeverTooLateDBHelper.Notifications.TYPE, notificationModel.type().ordinal());
        cv.put(NeverTooLateDBHelper.Notifications.SUBMISSION_ID, notificationModel.submission_id());

        context.getContentResolver().insert(NeverTooLateContract.NOTIFICATIONS_CONTENT_URI, cv);
    }

    public static void deleteNotification(Context context, NotificationModel notificationModel) {
        String selection = NeverTooLateDBHelper.Notifications._ID + " = \"" + notificationModel.id() + "\"";
        context.getContentResolver().delete(NeverTooLateContract.NOTIFICATIONS_CONTENT_URI, selection, null);
    }
}
