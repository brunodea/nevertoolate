package br.brunodea.nevertoolate.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = Motivation.class,
                                  parentColumns = "id",
                                  childColumns = "motivationId",
                                  onDelete = CASCADE))
public class MotivationalImageFromReddit {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public final String image_url;
    @NonNull
    public final String reddit_id;
    @NonNull
    public final String reddit_permalink;
    @NonNull
    public final String title;
    @NonNull
    public final int motivationId; // Id from Motivation table

    public MotivationalImageFromReddit(final int motivationId,
                                       final String permalink, final String image_url,
                                       final String reddit_id, final String title) {
        this.motivationId = motivationId;
        this.reddit_permalink = permalink;
        this.image_url = image_url;
        this.reddit_id = reddit_id;
        this.title = title;
    }
}
