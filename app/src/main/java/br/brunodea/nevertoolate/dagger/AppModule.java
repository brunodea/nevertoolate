package br.brunodea.nevertoolate.dagger;

import android.content.Context;

import javax.inject.Singleton;

import br.brunodea.nevertoolate.NeverTooLateApp;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private final NeverTooLateApp mNeverTooLateApp;
    public AppModule(final NeverTooLateApp app) {
        mNeverTooLateApp = app;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return mNeverTooLateApp;
    }
}
