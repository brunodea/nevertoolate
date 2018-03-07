package br.brunodea.nevertoolate.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Notification {
    @PrimaryKey(autoGenerate = true)
    public long id;

    enum NotificationType {
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
}
