package br.brunodea.nevertoolate.util;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.squareup.moshi.JsonAdapter;

import net.dean.jraw.JrawUtils;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;

import java.io.IOException;
import java.util.Iterator;

import br.brunodea.nevertoolate.NeverTooLateApp;

public class RedditUtils {
    private static final JsonAdapter<Submission> sSubmissionAdapter = JrawUtils.moshi.adapter(Submission.class).serializeNulls();

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
                    try {
                        Listing<Submission> next_posts = getMotivated.next();
                        // we look all pages until we either find the minimum number of posts or if the
                        // next page has no posts.
                        if (next_posts.size() == 0) {
                            break;
                        }
                        posts.addAll(next_posts);
                    } catch (Exception e) {
                        // getMotivated.next() may throw some exception, due to timeout or something;
                        // here we just make sure the app continues to work gracefully.
                        e.printStackTrace();
                        break;
                    }
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

    public static String handleRedditURL(String url) {
        if (url.contains("imgur")) {
            // If the link is for imgur, we need to change it to the address of the image location itself.
            // By appending a lowercase L to the imgur's image hash, we get a smaller image
            if (url.contains("/imgur")) {
                url = url.replace("/imgur", "/i.imgur");
                url += "l.jpg";
            } else {
                String ext = url.substring(url.lastIndexOf("."), url.length());
                url = url.replace(ext, "l" + ext);
            }
        }
        return url;
    }
    public static String handleRedditTitle(String title) {
        // Remove the tag from the title and capitalize its first letter.
        title = title.replace(
                title.substring(title.indexOf("["), title.indexOf("]") + 1),
                ""
        ).trim();
        if (title.length() > 1) {
            title = title.substring(0, 1).toUpperCase() + title.substring(1);
        }

        return title;
    }

    public static String toString(Submission submission) {
        return sSubmissionAdapter.toJson(submission);
    }
    public static Submission fromString(String json) {
        try {
            return sSubmissionAdapter.fromJson(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface RedditLoadingListener {
        void finishedLoading(Listing<Submission> submissions);
    }
}
