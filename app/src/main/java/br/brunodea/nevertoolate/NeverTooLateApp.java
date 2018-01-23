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
    public static RedditClient redditClient() {
        UUID deviceUuid = UUID.randomUUID();
        UserAgent userAgent = new UserAgent("android", "br.brunodea.nevertoolate", "v0.1", "brunodea");
        NetworkAdapter networkAdapter = new OkHttpNetworkAdapter(userAgent);
        return OAuthHelper.automatic(networkAdapter, Credentials.userlessApp("x7JYIvlBrQUlEw", deviceUuid));
    }
}
