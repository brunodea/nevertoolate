package br.brunodea.nevertoolate.db.dao;

import android.arch.persistence.room.Dao;

import br.brunodea.nevertoolate.db.entity.Notification;

@Dao
public interface NotificationDao extends EntityDao<Notification> {
}
