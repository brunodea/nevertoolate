package br.brunodea.nevertoolate.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import br.brunodea.nevertoolate.db.entity.Notification;

@Dao
public interface NotificationDao extends EntityDao<Notification> {
    @Query("SELECT * FROM notification")
    LiveData<List<Notification>> all();

    @Query("SELECT * FROM notification WHERE motivationId=:motivation_id")
    Notification findByMotivationId(final long motivation_id);

    @Query("SELECT * FROM notification WHERE id=:id")
    Notification findById(final long id);
}
