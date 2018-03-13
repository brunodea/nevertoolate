package br.brunodea.nevertoolate.frag.list;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.join.MotivationRedditImageJoin;

public class FavoritesListViewModel extends AndroidViewModel {
    private LiveData<List<MotivationRedditImageJoin>> mModelList;

    public FavoritesListViewModel(@NonNull Application application) {
        super(application);
        NeverTooLateDatabase db = NeverTooLateDatabase.getInstance(application);
        mModelList = db.getMotivationRedditImageDao().findAllFavoriteRedditImages();
    }

    public LiveData<List<MotivationRedditImageJoin>> getModelList() {
        return mModelList;
    }
}
