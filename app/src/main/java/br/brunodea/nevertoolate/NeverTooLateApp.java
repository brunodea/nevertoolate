package br.brunodea.nevertoolate;

import android.app.Application;

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
            UserAgent userAgent = new UserAgent("android", "br.brunodea.nevertoolate", "v0.1", "brunodea");
            NetworkAdapter networkAdapter = new OkHttpNetworkAdapter(userAgent);
            sRedditClient = OAuthHelper.automatic(networkAdapter, Credentials.userlessApp("x7JYIvlBrQUlEw", deviceUuid));
            sRedditClient.setAutoRenew(true);
        }

        return sRedditClient;
    }
}
