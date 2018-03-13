package br.brunodea.nevertoolate.frag.list;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.entity.Notification;

public class NotificationsListViewModel extends AndroidViewModel {
    private LiveData<List<Notification>> mModelList;

    public NotificationsListViewModel(@NonNull Application application) {
        super(application);
        NeverTooLateDatabase db = NeverTooLateDatabase.getInstance(application);
        mModelList = db.getNotificationDao().all();
    }

    public LiveData<List<Notification>> getModelList() {
        return mModelList;
    }
}
