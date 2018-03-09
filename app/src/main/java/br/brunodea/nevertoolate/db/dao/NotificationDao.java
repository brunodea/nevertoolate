package br.brunodea.nevertoolate.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import br.brunodea.nevertoolate.db.entity.Notification;

@Dao
public interface NotificationDao extends EntityDao<Notification> {
    @Query("SELECT * FROM notification")
    List<Notification> all();
}
