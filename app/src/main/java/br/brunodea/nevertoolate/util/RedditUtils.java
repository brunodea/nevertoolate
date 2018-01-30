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

    public static void queryGetMotivated(RedditLoadingListener listener, int min_posts) {
        new ReauthenticationTask(listener, min_posts).execute();
    }

    private static class ReauthenticationTask extends AsyncTask<Void, Void, Listing<Submission>> {
        private RedditLoadingListener mRedditLoadingListener;
        private int mMinimumNumberOfPosts;

        public ReauthenticationTask(RedditLoadingListener listener, int min_posts) {
            mRedditLoadingListener = listener;
            mMinimumNumberOfPosts = min_posts;
        }

        @Override
        protected Listing<Submission> doInBackground(Void... voids) {
            DefaultPaginator<Submission> getMotivated = NeverTooLateApp.redditClient()
                    .subreddit("GetMotivated")
                    .posts()
                    .sorting(SubredditSort.HOT)
                    .timePeriod(TimePeriod.DAY)
                    .limit(mMinimumNumberOfPosts * 2)
                    .build();
            Listing<Submission> posts = getMotivated.next();
            do {
                // only use posts with the tag "[image]" and do not use imgur albums
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    posts.removeIf(p -> !p.getTitle().toLowerCase().contains("[image]"));
                    posts.removeIf(p -> p.getUrl().contains("imgur.com/a/"));
                } else {
                    for (Iterator<Submission> it = posts.iterator(); it.hasNext(); ) {
                        Submission s = it.next();
                        if (!s.getTitle().toLowerCase().contains("[image]") ||
                                s.getUrl().contains("imgur.com/a/")) {
                            it.remove();
                        }
                    }
                }
                if (posts.size() < mMinimumNumberOfPosts) {
                    Listing<Submission> next_posts = getMotivated.next();
                    if (next_posts.size() == 0) {
                        break;
                    }
                    posts.addAll(next_posts);
                } else {
                    break;
                }
            } while(true);
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
