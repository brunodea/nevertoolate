package br.brunodea.nevertoolate.util;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.dao.MotivationDao;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;
import br.brunodea.nevertoolate.db.entity.Notification;
import br.brunodea.nevertoolate.model.NotificationModel;
import br.brunodea.nevertoolate.model.SubmissionParcelable;

public class NeverTooLateDBUtil {
    public static boolean isFavorite(final Context context,  final SubmissionParcelable submission) {
        NeverTooLateDatabase db = NeverTooLateDatabase.getInstance(context);
        LiveData<List<Motivation>> favorites = db.getMotivationDao().findAllFavorites();
        if (favorites != null) {
            List<Motivation> favs = favorites.getValue();
            if (favs != null) {
                for (Motivation m : favs) {
                    switch (m.type) {
                        case REDDIT_IMAGE:
                            MotivationRedditImage reddit_image = db
                                    .getMotivationRedditImageDao()
                                    .findByRedditId(submission.id());
                            if (reddit_image != null) {
                                return true;
                            }
                    }
                }
            }
        }
        return false;
    }

    public static SubmissionParcelable from(final NeverTooLateDatabase db, final Motivation motivation) {
        MotivationRedditImage motivation_reddit_image = db.getMotivationRedditImageDao()
                .findById(motivation.motivationId);
        SubmissionParcelable submissionParcelable = null;

        if (motivation_reddit_image != null) {
            submissionParcelable = new SubmissionParcelable();
            submissionParcelable.setTitle(motivation_reddit_image.title);
            submissionParcelable.setURL(motivation_reddit_image.image_url);
            submissionParcelable.setID(motivation_reddit_image.reddit_id);
            submissionParcelable.setPermalink(motivation_reddit_image.reddit_permalink);
        }

        return submissionParcelable;
    }

    /*
    public static void queryMotivationsBy(final String reddit_id, final QueryMotivationResultListener resultListener) {
    }

    public interface QueryMotivationResultListener {
        void onNothing();
        void onResult(List<Motivation> motivations);
        void onLiveDataResult(LiveData<List<Motivation>> motivations);
    }

    private static class QueryMotivationAsyncTask extends AsyncTask<QueryMotivationAsyncTask.Action, Void, Void> {
        private NeverTooLateDatabase mDB;
        private QueryMotivationResultListener mResultListener;
        enum Action {
            BY_ID,
            BY_REDDIT_ID,
            LIVE_DATA_ALL_FAVORITES
        }

        long mID;

        private QueryMotivationAsyncTask(NeverTooLateDatabase db,
                                         QueryMotivationResultListener result_listener) {
            mDB = db;
            mResultListener = result_listener;
        }

        @Override
        protected Void doInBackground(Action... actions) {
            MotivationDao dao = mDB.getMotivationDao();
            for (Action action : actions) {
                switch (action) {
                    case BY_ID:
                        Motivation m = dao.findbyId()
                        break;
                    case BY_REDDIT_ID:
                        break;
                    case LIVE_DATA_ALL_FAVORITES:
                        break;
                }
            }
            return null;
        }
    }*/
}
