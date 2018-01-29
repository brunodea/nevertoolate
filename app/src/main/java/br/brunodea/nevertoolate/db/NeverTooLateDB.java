package br.brunodea.nevertoolate.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import br.brunodea.nevertoolate.model.SubmissionParcelable;

public class NeverTooLateDB {
    private static SubmissionParcelable findSubmissionByRedditID(Context context, String reddit_id) {
        String selection = NeverTooLateDBHelper.Favorites.REDDIT_ID + " = \"" + reddit_id + "\"";
        Cursor c = context.getContentResolver().query(NeverTooLateContract.FAVORITES_CONTENT_URI,
                NeverTooLateDBHelper.Favorites.PROJECTION_ALL,
                selection,
                null, null);
        SubmissionParcelable result = null;
        if (c != null) {
            if (c.moveToFirst()) {
                fromFavoritesTableCursor(c);
            }
            c.close();
        }
        return result;
    }
    public static SubmissionParcelable fromFavoritesTableCursor(Cursor cursor) {
        SubmissionParcelable res = null;
        if (cursor != null) {
            String url = cursor.getString(0);
            String permalink = cursor.getString(1);
            String title = cursor.getString(2);
            String reddit_id = cursor.getString(3);

            res = new SubmissionParcelable();
            res.setID(reddit_id);
            res.setURL(url);
            res.setPermalink(permalink);
            res.setTitle(title);
        }

        return res;
    }
    public static boolean isFavorite(Context context, SubmissionParcelable submission) {
        return findSubmissionByRedditID(context, submission.id()) != null;
    }
    public static void insertSubmission(Context context, SubmissionParcelable submission) {
        ContentValues cv = new ContentValues();
        cv.put(NeverTooLateDBHelper.Favorites.REDDIT_ID, submission.id());
        cv.put(NeverTooLateDBHelper.Favorites.URL, submission.url());
        cv.put(NeverTooLateDBHelper.Favorites.PERMALINK, submission.permalink());
        cv.put(NeverTooLateDBHelper.Favorites.TITLE, submission.title());

        context.getContentResolver().insert(NeverTooLateContract.FAVORITES_CONTENT_URI, cv);
    }
    public static void deleteSubmission(Context context, SubmissionParcelable submission) {
        String selection = NeverTooLateDBHelper.Favorites.REDDIT_ID + " = \"" + submission.id() + "\"";
        context.getContentResolver().delete(NeverTooLateContract.FAVORITES_CONTENT_URI, selection, null);
    }
}
