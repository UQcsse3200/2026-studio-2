package com.csse3200.game.components.npc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(GameExtension.class)
class CalypsoSoundComponentTest {
  private ResourceService resources;
  private Entity calypso;

  @BeforeEach
  void setUp() {
    resources = mock(ResourceService.class);
    when(resources.containsAsset(anyString(), eq(Sound.class))).thenReturn(true);
    Sound sound = mock(Sound.class);
    when(resources.getAsset(anyString(), eq(Sound.class))).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);
    calypso = new Entity().addComponent(new CalypsoSoundComponent());
    calypso.create();
  }

  @ParameterizedTest
  @CsvSource({
    "attackStart, sounds/calypso_windup.ogg",
    "sweepHit, sounds/calypso_hit.ogg",
    "hitPlayer, sounds/calypso_teleport.ogg",
    "hurt, sounds/calypso_hurt.ogg",
    "beginBossPhaseChange, sounds/calypso_phase_start.ogg"
  })
  void shouldPlayTheRightSoundForEachEvent(String event, String sound) {
    calypso.getEvents().trigger(event);

    verify(resources).getAsset(sound, Sound.class);
  }

  @Test
  void shouldPlayPhaseTwoSoundWhenThePhaseChanges() {
    calypso.getEvents().trigger("bossPhaseChanged", 2);

    verify(resources).getAsset("sounds/calypso_phase2.ogg", Sound.class);
  }

  @Test
  void shouldLeaveTheDeathSoundToEnemyDeathComponent() {
    calypso.getEvents().trigger("enemyDied", calypso);

    verify(resources, never()).getAsset("sounds/calypso_death.ogg", Sound.class);
  }

  @Test
  void shouldNotSpamTheTeleportJingle() {
    GameTime time = mock(GameTime.class);
    when(time.getTime()).thenReturn(0L, 500L, 2500L);
    ServiceLocator.registerTimeSource(time);

    calypso.getEvents().trigger("hitPlayer");
    calypso.getEvents().trigger("hitPlayer");
    calypso.getEvents().trigger("hitPlayer");

    verify(resources, times(2)).getAsset("sounds/calypso_teleport.ogg", Sound.class);
  }
}
