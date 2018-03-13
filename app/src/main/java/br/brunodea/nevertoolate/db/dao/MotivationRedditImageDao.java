package br.brunodea.nevertoolate.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;
import br.brunodea.nevertoolate.db.join.MotivationRedditImageJoin;

@Dao
public interface MotivationRedditImageDao extends EntityDao<MotivationRedditImage> {
    @Query("SELECT * FROM motivation_reddit_image WHERE reddit_id=:reddit_id")
    MotivationRedditImage findByRedditId(final String reddit_id);

    @Query("SELECT * FROM motivation_reddit_image WHERE motivation_reddit_image_id=:id")
    MotivationRedditImage findById(final long id);

    @Query("SELECT * FROM motivation_reddit_image WHERE parent_motivation_id=:motivation_id")
    MotivationRedditImage findByMotivationId(final long motivation_id);

    @Query("SELECT motivation.*, motivation_reddit_image.* FROM motivation INNER JOIN motivation_reddit_image" +
            " ON motivation.child_motivation_id = motivation_reddit_image.motivation_reddit_image_id WHERE motivation.favorite=1")
    LiveData<List<MotivationRedditImageJoin>> findAllFavoriteRedditImages();
}
