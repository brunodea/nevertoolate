package br.brunodea.nevertoolate.util;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import net.dean.jraw.models.Submission;

import java.util.List;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;
import br.brunodea.nevertoolate.db.entity.Notification;

public class NeverTooLateDBUtil {
    public static boolean isFavorite(final Context context,  final Submission submission) {
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
                                    .findByRedditId(submission.getId());
                            if (reddit_image != null) {
                                return true;
                            }
                    }
                }
            }
        }
        return false;
    }
}
