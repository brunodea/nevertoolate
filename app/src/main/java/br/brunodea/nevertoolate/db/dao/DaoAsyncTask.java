package br.brunodea.nevertoolate.db.dao;

import android.os.AsyncTask;

public class DaoAsyncTask<Model> extends AsyncTask<Model, Void, Void> {
    public enum Action {
        INSERT,
        DELETE
    }

    private EntityDao<Model> mDao;
    private Action mAction;

    public DaoAsyncTask(EntityDao<Model> dao, Action action) {
        mDao = dao;
        mAction = action;
    }

    @Override
    protected Void doInBackground(Model[] models) {
        for (Model m : models) {
            switch (mAction) {
                case INSERT:
                    mDao.insert(m);
                    break;
                case DELETE:
                    mDao.delete(m);
                    break;
            }
        }
        return null;
    }
}
