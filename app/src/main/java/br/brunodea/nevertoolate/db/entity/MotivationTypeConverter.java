package br.brunodea.nevertoolate.db.entity;

import android.arch.persistence.room.TypeConverter;

public class MotivationTypeConverter {
    @TypeConverter
    public static Motivation.MotivationType toMotivationType(int motivationType) {
        if (motivationType == Motivation.MotivationType.IMAGE.code()) {
            return Motivation.MotivationType.IMAGE;
        } else {
            throw new IllegalArgumentException("Could not recognize motivation type: " + motivationType);
        }
    }

    @TypeConverter
    public static int toInteger(Motivation.MotivationType motivationType) {
        return motivationType.code();
    }
}
