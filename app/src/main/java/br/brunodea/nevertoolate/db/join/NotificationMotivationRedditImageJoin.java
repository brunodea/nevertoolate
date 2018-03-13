package br.brunodea.nevertoolate.db.join;

import android.arch.persistence.room.Embedded;

import br.brunodea.nevertoolate.db.entity.MotivationRedditImage;
import br.brunodea.nevertoolate.db.entity.Notification;

public class NotificationMotivationRedditImageJoin {
    @Embedded
    public Notification notification;
    @Embedded
    public MotivationRedditImage motivation_reddit_image;
}
