package br.brunodea.nevertoolate.util;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import java.util.List;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
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
            for (Motivation m : favorites.getValue()) {
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
}
