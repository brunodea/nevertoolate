package br.brunodea.nevertoolate.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;

@Dao
public interface MotivationRedditImageDao extends EntityDao<MotivationRedditImage> {
    @Query("SELECT * FROM motivation_reddit_image WHERE reddit_id=:reddit_id")
    MotivationRedditImage findByRedditId(final String reddit_id);

    @Query("SELECT * FROM motivation_reddit_image WHERE id=:id")
    MotivationRedditImage findById(final long id);
}
