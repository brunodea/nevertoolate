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
                String url = c.getString(0);
                String permalink = c.getString(1);
                String title = c.getString(2);

                result = new SubmissionParcelable();
                result.setID(reddit_id);
                result.setURL(url);
                result.setPermalink(permalink);
                result.setTitle(title);
            }
            c.close();
        }
        return result;
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
