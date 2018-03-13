package br.brunodea.nevertoolate.dagger;

import android.arch.persistence.room.Room;
import android.content.Context;

import javax.inject.Singleton;

import br.brunodea.nevertoolate.db.NeverTooLateDatabase;
import dagger.Module;
import dagger.Provides;

@Module
public class RoomModule {
    private static final String DB_NAME = "nevertoolate.db";

    @Provides
    @Singleton
    NeverTooLateDatabase providesDatabase(Context context) {
        return Room.databaseBuilder(
                context,
                NeverTooLateDatabase.class,
                DB_NAME).build();
    }
}
