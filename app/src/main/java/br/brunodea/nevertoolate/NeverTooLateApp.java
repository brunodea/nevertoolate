package br.brunodea.nevertoolate;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;

import java.util.UUID;

public class NeverTooLateApp extends Application {
    private static RedditClient sRedditClient = null;
    public static RedditClient redditClient() {
        if (sRedditClient == null) {
            UUID deviceUuid = UUID.randomUUID();
            UserAgent userAgent = new UserAgent("android", "br.brunodea.nevertoolate", "v1.0", BuildConfig.REDDIT_ACCOUNT);
            NetworkAdapter networkAdapter = new OkHttpNetworkAdapter(userAgent);
            sRedditClient = OAuthHelper.automatic(networkAdapter, Credentials.userlessApp(BuildConfig.REDDIT_CLIENT_ID, deviceUuid));
            sRedditClient.setAutoRenew(true);
        }

        return sRedditClient;
    }
    private static GoogleApiClient sGoogleApiClient;
    public static GoogleApiClient googleClient(FragmentActivity activity, GoogleApiClient.OnConnectionFailedListener failure_callback) {
        if (sGoogleApiClient == null) {
            sGoogleApiClient = new GoogleApiClient
                    .Builder(activity)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(activity, failure_callback)
                    .build();
        }
        return sGoogleApiClient;
    }
}
