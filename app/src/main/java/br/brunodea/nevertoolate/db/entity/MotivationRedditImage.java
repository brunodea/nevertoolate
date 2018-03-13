package br.brunodea.nevertoolate.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = Motivation.class,
                                  parentColumns = "motivation_id",
                                  childColumns = "parent_motivation_id",
                                  onDelete = CASCADE),
        tableName = "motivation_reddit_image")
public class MotivationRedditImage {
    @PrimaryKey(autoGenerate = true)
    public long motivation_reddit_image_id;
    @NonNull
    public final String reddit_permalink;
    @NonNull
    public final String image_url;
    @NonNull
    public final String reddit_id;
    @NonNull
    public final String title;
    public final String submission_json;
    public long parent_motivation_id; // Id from Motivation table

    public MotivationRedditImage(@NonNull final String reddit_permalink, @NonNull final String image_url,
                                 @NonNull final String reddit_id, @NonNull final String title,
                                 final String submission_json, long parent_motivation_id) {
        this.parent_motivation_id = parent_motivation_id;
        this.reddit_permalink = reddit_permalink;
        this.image_url = image_url;
        this.reddit_id = reddit_id;
        this.submission_json = submission_json;
        this.title = title;
    }
}
