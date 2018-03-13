package br.brunodea.nevertoolate.db;

import android.support.v4.util.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;

import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;

@Singleton
public class DBExec {
    @Inject
    NeverTooLateDatabase mDB;

    @Inject
    public DBExec() {
    }

    public void insertMotivationRedditImage(Motivation parent_motivation,
                                            MotivationRedditImage reddit_motivation,
                                            QueryInterface<Pair<Motivation, MotivationRedditImage>> result_interface) {
        Pair<Motivation, MotivationRedditImage> result = mDB.runInTransaction(() -> {
            long motivation_id = mDB.getMotivationDao().insert(parent_motivation);
            reddit_motivation.parent_motivation_id = motivation_id;
            long motivation_reddit_image_id =
                    mDB.getMotivationRedditImageDao().insert(reddit_motivation);
            parent_motivation.motivation_id = motivation_id;
            parent_motivation.child_motivation_id = motivation_reddit_image_id;
            mDB.getMotivationDao().update(parent_motivation);
            return Pair.create(parent_motivation, reddit_motivation);
        });
        if (result_interface != null) {
            result_interface.onResult(result);
        }
    }
    public void deleteMotivationRedditImage(Motivation parent_motivation,
                                            MotivationRedditImage reddit_motivation) {
        mDB.runInTransaction(() -> mDB.getMotivationRedditImageDao().delete(reddit_motivation));
    }
    public void updateMotivationRedditImage(Motivation parent_motivation,
                                            MotivationRedditImage reddit_motivation) {
        mDB.runInTransaction(() -> {
            mDB.getMotivationDao().update(parent_motivation);
            mDB.getMotivationRedditImageDao().update(reddit_motivation);
        });
    }


    public interface QueryInterface<T> {
        void onResult(T result);
    }
}
