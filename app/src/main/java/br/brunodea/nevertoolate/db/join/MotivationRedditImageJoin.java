package br.brunodea.nevertoolate.db.join;

import android.arch.persistence.room.Embedded;

import br.brunodea.nevertoolate.db.entity.Motivation;
import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;

public class MotivationRedditImageJoin {
    @Embedded
    public Motivation motivation;
    @Embedded
    public MotivationRedditImage motivation_reddit_image;
}
