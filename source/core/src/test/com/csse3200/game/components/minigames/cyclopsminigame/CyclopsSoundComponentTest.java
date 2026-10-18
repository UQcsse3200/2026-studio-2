package com.csse3200.game.components.minigames.cyclopsminigame;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CyclopsSoundComponentTest {
  private final Sound snore = mock(Sound.class);
  private final Sound roar = mock(Sound.class);
  private final Sound landing = mock(Sound.class);
  private final Sound clubHit = mock(Sound.class);
  private Entity cyclops;

  @BeforeEach
  void beforeEach() {
    ResourceService resources = mock(ResourceService.class);
    when(resources.getAsset(CyclopsSoundComponent.SNORE_PATH, Sound.class)).thenReturn(snore);
    when(resources.getAsset(CyclopsSoundComponent.ROAR_PATH, Sound.class)).thenReturn(roar);
    when(resources.getAsset(CyclopsSoundComponent.LANDING_PATH, Sound.class)).thenReturn(landing);
    when(resources.getAsset(CyclopsSoundComponent.CLUB_HIT_PATH, Sound.class)).thenReturn(clubHit);
    ServiceLocator.registerResourceService(resources);

    CyclopsSoundComponent component = new CyclopsSoundComponent();
    cyclops = new Entity().addComponent(component);
    component.create();
  }

  @AfterEach
  void afterEach() {
    ServiceLocator.clear();
  }

  @Test
  void snoresWhileAsleep() {
    verify(snore).loop(CyclopsSoundComponent.SNORE_VOLUME);
    verify(snore, never()).stop();
  }

  @Test
  void stopsSnoringAndRoarsWhenHeWakes() {
    cyclops.getEvents().trigger(CyclopsAnimationController.WOKE_UP_EVENT);

    verify(snore).stop();
    verify(roar).play(CyclopsSoundComponent.ROAR_VOLUME);
  }

  @Test
  void thudsWhenHeLands() {
    cyclops.getEvents().trigger(CyclopsAnimationController.LANDED_EVENT);

    verify(landing).play(CyclopsSoundComponent.LANDING_VOLUME);
  }

  @Test
  void cracksWhenHisClubLands() {
    cyclops.getEvents().trigger(CyclopsAnimationController.CLUB_LANDED_EVENT);

    verify(clubHit).play(CyclopsSoundComponent.CLUB_HIT_VOLUME);
  }

  @Test
  void snoresAgainWhenHeFallsBackAsleep() {
    cyclops.getEvents().trigger(CyclopsAnimationController.WOKE_UP_EVENT);
    cyclops.getEvents().trigger(CyclopsAnimationController.FELL_ASLEEP_EVENT);

    verify(snore, times(2)).loop(CyclopsSoundComponent.SNORE_VOLUME);
  }
}
