package br.brunodea.nevertoolate.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import br.brunodea.nevertoolate.db.entity.Motivation;

@Dao
public interface MotivationDao extends EntityDao<Motivation> {
    @Query("SELECT * FROM motivation WHERE id=:id")
    Motivation findbyId(final long id);

    @Query("SELECT * FROM motivation WHERE favorite=1")
    List<Motivation> findAllFavorites();

    @Query("SELECT * FROM motivation WHERE notificationId=:notificationId")
    Motivation findByNotificationId(final long notificationId);
}
