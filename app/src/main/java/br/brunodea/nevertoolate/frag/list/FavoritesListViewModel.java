package br.brunodea.nevertoolate.frag.list;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.entity.Motivation;

public class FavoritesListViewModel extends AndroidViewModel {
    private LiveData<List<Motivation>> mModelList;

    public FavoritesListViewModel(@NonNull Application application) {
        super(application);
        NeverTooLateDatabase db = NeverTooLateDatabase.getInstance(application);
        mModelList = db.getMotivationDao().findAllFavorites();
    }

    public LiveData<List<Motivation>> getModelList() {
        return mModelList;
    }
}
