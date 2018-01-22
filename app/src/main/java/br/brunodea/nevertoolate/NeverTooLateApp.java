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
    //private static AccountHelper sAccountHelper;
    //private static SharedPreferencesTokenStore sTokenStore;

    /*
    @Override
    public void onCreate() {
        super.onCreate();

        // Get UserAgent and OAuth2 data from AndroidManifest.xml
        AppInfoProvider provider = new ManifestAppInfoProvider(getApplicationContext());

        // Ideally, this should be unique to every device
        UUID deviceUuid = UUID.randomUUID();

        // Store our access tokens and refresh tokens in shared preferences
        sTokenStore = new SharedPreferencesTokenStore(getApplicationContext());
        // Load stored tokens into memory
        sTokenStore.load();
        // Automatically save new tokens as they arrive
        sTokenStore.setAutoPersist(true);

        // An AccountHelper manages switching between accounts and into/out of userless mode.
        sAccountHelper = AndroidHelper.accountHelper(provider, deviceUuid, sTokenStore);

        // Every time we use the AccountHelper to switch between accounts (from one account to
        // another, or into/out of userless mode), call this function
        sAccountHelper.onSwitch(redditClient -> {
            // By default, JRAW logs HTTP activity to System.out. We're going to use Log.i()
            // instead.
            LogAdapter logAdapter = new SimpleAndroidLogAdapter(Log.INFO);

            // We're going to use the LogAdapter to write down the summaries produced by
            // SimpleHttpLogger
            redditClient.setLogger(
                    new SimpleHttpLogger(SimpleHttpLogger.DEFAULT_LINE_LENGTH, logAdapter));

            // If you want to disable logging, use a NoopHttpLogger instead:
            // redditClient.setLogger(new NoopHttpLogger());

            return null;
        });
    }*/
    public static RedditClient redditClient() {
        UUID deviceUuid = UUID.randomUUID();
        UserAgent userAgent = new UserAgent("android", "br.brunodea.nevertoolate", "v0.1", "brunodea");
        NetworkAdapter networkAdapter = new OkHttpNetworkAdapter(userAgent);
        return OAuthHelper.automatic(networkAdapter, Credentials.userlessApp("x7JYIvlBrQUlEw", deviceUuid));
    }

    //public static AccountHelper getAccountHelper() { return sAccountHelper; }
    //public static SharedPreferencesTokenStore getTokenStore() { return sTokenStore; }
}
