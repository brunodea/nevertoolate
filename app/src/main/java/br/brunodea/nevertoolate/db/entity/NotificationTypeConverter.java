package br.brunodea.nevertoolate.db.entity;

import android.arch.persistence.room.TypeConverter;

public class NotificationTypeConverter {
    @TypeConverter
    public static Notification.NotificationType toNotificationType(int notificationType) {
        if (notificationType == Notification.NotificationType.TIME.code()) {
            return Notification.NotificationType.TIME;
        } else if (notificationType == Notification.NotificationType.GEOFENCE.code()) {
            return Notification.NotificationType.GEOFENCE;
        } else {
            throw new IllegalArgumentException("Could not recognize notification type: " + notificationType);
        }
    }
    @TypeConverter
    public static int toInteger(Notification.NotificationType notificationType) {
        return notificationType.code();
    }
}
