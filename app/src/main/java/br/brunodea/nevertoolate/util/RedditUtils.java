package br.brunodea.nevertoolate.util;

import android.os.AsyncTask;
import android.os.Build;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;

import java.util.Iterator;

import br.brunodea.nevertoolate.NeverTooLateApp;

public class RedditUtils {

    public static void queryGetMotivated(RedditLoadingListener listener) {
        new ReauthenticationTask(listener).execute();
    }

    private static class ReauthenticationTask extends AsyncTask<Void, Void, Listing<Submission>> {
        private RedditLoadingListener mRedditLoadingListener;

        public ReauthenticationTask(RedditLoadingListener listener) {
            mRedditLoadingListener = listener;
        }

        @Override
        protected Listing<Submission> doInBackground(Void... voids) {
            DefaultPaginator<Submission> getMotivated = NeverTooLateApp.redditClient()
                    .subreddit("GetMotivated")
                    .posts()
                    .sorting(SubredditSort.HOT)
                    .timePeriod(TimePeriod.DAY)
                    .limit(50)
                    .build();
            Listing<Submission> posts = getMotivated.next();
            // only use posts with the tag "[image]" and do not use imgur albums
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                posts.removeIf(p -> !p.getTitle().toLowerCase().contains("[image]"));
                posts.removeIf(p -> p.getUrl().contains("imgur.com/a/"));
            } else {
                for (Iterator<Submission> it = posts.iterator(); it.hasNext();) {
                    Submission s = it.next();
                    if (!s.getTitle().toLowerCase().contains("[image]") ||
                            s.getUrl().contains("imgur.com/a/")) {
                        it.remove();
                    }
                }
            }
            return posts;
        }
        @Override
        protected void onPostExecute(Listing<Submission> submissions) {
            if (mRedditLoadingListener != null) {
                mRedditLoadingListener.finishedLoading(submissions);
            }
        }
    }

    public interface RedditLoadingListener {
        void finishedLoading(Listing<Submission> submissions);
    }
}
