package br.brunodea.nevertoolate.db;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

public class NeverTooLateContract {
    private static final String AUTHORITY = "br.brunodea.nevertoolate.db.NeverTooLateProvider";
    private static final String FAVORITES_BASE_PATH = "favorites";
    private static final String FAVORITES_URL = "content://" + AUTHORITY + "/" + FAVORITES_BASE_PATH;
    private static final String NOTIFICATIONS_BASE_PATH = "notifications";
    private static final String NOTIFICATIONS_URL = "content://" + AUTHORITY + "/" + NOTIFICATIONS_BASE_PATH;
    public static final Uri FAVORITES_CONTENT_URI = Uri.parse(FAVORITES_URL);
    public static final Uri NOTIFICATIONS_CONTENT_URI = Uri.parse(NOTIFICATIONS_URL);

    static final int FAVORITES = 10;
    static final int FAVORITES_ID = 11;
    static final int NOTIFICATIONS = 20;
    static final int NOTIFICATIONS_ID = 21;
    static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, FAVORITES_BASE_PATH, FAVORITES);
        URI_MATCHER.addURI(AUTHORITY, FAVORITES_BASE_PATH + "/#", FAVORITES_ID);
        URI_MATCHER.addURI(AUTHORITY, NOTIFICATIONS_BASE_PATH, NOTIFICATIONS);
        URI_MATCHER.addURI(AUTHORITY, NOTIFICATIONS_BASE_PATH + "/#", NOTIFICATIONS_ID);
    }

    static final String FAVORITES_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/favorites";
    static final String FAVORITES_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/favorites";
    static final String NOTIFICATIONS_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/notifications";
    static final String NOTIFICATIONS_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/notifications";
}
