package br.brunodea.nevertoolate.util;

import android.content.Context;

import java.util.List;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;
import br.brunodea.nevertoolate.model.SubmissionParcelable;

public class NeverTooLateDBUtil {
    public static boolean isFavorite(final Context context,  final SubmissionParcelable submission) {
        NeverTooLateDatabase db = NeverTooLateDatabase.getInstance(context);
        List<Motivation> favorites = db
                .getMotivationDao()
                .findAllFavorites();
        for (Motivation m : favorites) {
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
        return false;
    }
}
