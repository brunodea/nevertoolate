package br.brunodea.nevertoolate.dagger;

import javax.inject.Singleton;

import br.brunodea.nevertoolate.frag.HomeFragment;
import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class,
        RoomModule.class})
public interface NeverTooLateComponent {
    void injectHomeFragment(HomeFragment fragment);
}
