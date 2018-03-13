package br.brunodea.nevertoolate.db.dao;

import android.os.AsyncTask;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;

public class MotivationRedditImageDaoAsyncTask extends AsyncTask<Void, Void, Void> {
    public enum Action {
        INSERT,
        DELETE,
        UPDATE
    }

    private Action mAction;
    private NeverTooLateDatabase mDB;

    private List<Pair<Motivation, MotivationRedditImage>> mMotivationPairList;

    public interface InsertListener {
        void onInsert(final long motivation_id, final long reddit_image_id);
    }
    private InsertListener mListener;

    public MotivationRedditImageDaoAsyncTask(Motivation motivation,
                                             MotivationRedditImage motivation_reddit_image,
                                             NeverTooLateDatabase db,
                                             Action action) {
        mAction = action;
        mDB = db;
        mMotivationPairList = new ArrayList<>();
        mMotivationPairList.add(Pair.create(motivation, motivation_reddit_image));
        mListener = null;
    }

    public MotivationRedditImageDaoAsyncTask(List<Pair<Motivation, MotivationRedditImage>> list,
                                             NeverTooLateDatabase db,
                                             Action action) {
        mAction = action;
        mDB = db;
        mMotivationPairList = list;
        mListener = null;
    }

    public void setInsertListener(final InsertListener listener) {
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... nothing) {
        for (Pair<Motivation, MotivationRedditImage> p : mMotivationPairList) {
            Motivation motivation = p.first;
            MotivationRedditImage motivation_reddit_image = p.second;
            if (motivation != null && motivation_reddit_image != null) {
                switch (mAction) {
                    case INSERT:
                        long motivation_id = mDB.getMotivationDao().insert(motivation);
                        motivation_reddit_image.parent_motivation_id = motivation_id;
                        long motivation_reddit_image_id =
                                mDB.getMotivationRedditImageDao().insert(motivation_reddit_image);
                        motivation.motivation_id = motivation_id;
                        motivation.child_motivation_id = motivation_reddit_image_id;
                        mDB.getMotivationDao().update(motivation);
                        if (mListener != null) {
                            mListener.onInsert(motivation_id, motivation_reddit_image_id);
                        }
                        break;
                    case DELETE:
                        // TODO: make sure we don't need to delete motivation as well.
                        mDB.getMotivationRedditImageDao().delete(motivation_reddit_image);
                        break;
                    case UPDATE:
                        mDB.getMotivationDao().update(motivation);
                        mDB.getMotivationRedditImageDao().update(motivation_reddit_image);
                        break;
                }
            }
        }
        return null;
    }
}
