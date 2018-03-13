package br.brunodea.nevertoolate.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

@Entity(foreignKeys = @ForeignKey(entity = Motivation.class,
                                  parentColumns = "motivation_id",
                                  childColumns = "base_motivation_id"),
        tableName = "notification")
public class Notification {
    @PrimaryKey(autoGenerate = true)
    public long notification_id;
    @NonNull @TypeConverters(NotificationTypeConverter.class)
    public final NotificationType type;
    @NonNull
    public final String info;
    public long base_motivation_id; // Can be null -- notification not triggered yet.

    public enum NotificationType {
        TIME(0),
        GEOFENCE(1);

        private int code;
        NotificationType(int code) {
            this.code = code;
        }
        public int code() {
            return this.code;
        }
    }

    public Notification(@NonNull final NotificationType type, @NonNull final String info,
                        final long base_motivation_id) {
        this.type = type;
        this.info = info;
        this.base_motivation_id = base_motivation_id;
    }
}
