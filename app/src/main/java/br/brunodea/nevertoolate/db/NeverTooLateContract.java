package br.brunodea.nevertoolate.db;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;

public class NeverTooLateContract {
    private static final String AUTHORITY = "br.brunodea.nevertoolate.db.NeverTooLateProvider";
    private static final String FAVORITES_BASE_PATH = "favorites";
    private static final String FAVORITES_URL = "content://" + AUTHORITY + "/" + FAVORITES_BASE_PATH;
    public static final Uri FAVORITES_CONTENT_URI = Uri.parse(FAVORITES_URL);

    static final int FAVORITES = 10;
    static final int FAVORITES_ID = 11;
    static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, FAVORITES_BASE_PATH, FAVORITES);
        URI_MATCHER.addURI(AUTHORITY, FAVORITES_BASE_PATH + "/#", FAVORITES_ID);
    }

    public static final String FAVORITES_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/favorites";
    public static final String FAVORITES_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/favorites";
}
