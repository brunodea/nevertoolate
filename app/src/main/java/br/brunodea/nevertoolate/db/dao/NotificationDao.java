package br.brunodea.nevertoolate.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import br.brunodea.nevertoolate.db.entity.Notification;
import br.brunodea.nevertoolate.db.join.NotificationMotivationRedditImageJoin;

@Dao
public interface NotificationDao extends EntityDao<Notification> {
    @Query("SELECT * FROM notification")
    LiveData<List<Notification>> all();

    @Query("SELECT * FROM notification WHERE base_motivation_id=:motivation_id")
    Notification findByMotivationId(final long motivation_id);

    @Query("SELECT * FROM notification WHERE notification_id=:id")
    Notification findById(final long id);

    @Query("SELECT notification.*, motivation_reddit_image.* FROM notification JOIN motivation_reddit_image" +
            " ON notification.base_motivation_id = motivation_reddit_image.parent_motivation_id")
    LiveData<List<NotificationMotivationRedditImageJoin>> findAllNotifications();
}
