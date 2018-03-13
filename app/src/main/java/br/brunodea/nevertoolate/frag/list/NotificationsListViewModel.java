package br.brunodea.nevertoolate.frag.list;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import br.brunodea.nevertoolate.db.join.NotificationMotivationRedditImageJoin;

public class NotificationsListViewModel extends AndroidViewModel {
    private LiveData<List<NotificationMotivationRedditImageJoin>> mModelList;

    public NotificationsListViewModel(@NonNull Application application) {
        super(application);
        NeverTooLateDatabase db = NeverTooLateDatabase.getInstance(application);
        mModelList = db.getNotificationDao().findAllNotifications();
    }

    public LiveData<List<NotificationMotivationRedditImageJoin>> getModelList() {
        return mModelList;
    }
}
