package com.csse3200.game.components.npc;

import static org.mockito.ArgumentMatchers.anyFloat;
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

@ExtendWith(GameExtension.class)
class ChaseSoundComponentTest {
  private ResourceService resources;
  private Sound sound;
  private GameTime time;
  private Entity enemy;

  @BeforeEach
  void setUp() {
    resources = mock(ResourceService.class);
    sound = mock(Sound.class);
    when(resources.containsAsset(anyString(), eq(Sound.class))).thenReturn(true);
    when(resources.getAsset(anyString(), eq(Sound.class))).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);
    time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    enemy = new Entity().addComponent(new ChaseSoundComponent("sounds/osprey.ogg", 0.15f));
    enemy.create();
  }

  @Test
  void shouldPlaySoundWhenChaseStarts() {
    when(time.getTime()).thenReturn(0L);

    enemy.getEvents().trigger("chaseStart");

    verify(resources).getAsset("sounds/osprey.ogg", Sound.class);
    verify(sound).play(0.15f);
  }

  @Test
  void shouldNotPlayAgainDuringCooldown() {
    when(time.getTime()).thenReturn(0L, 5000L);

    enemy.getEvents().trigger("chaseStart");
    enemy.getEvents().trigger("chaseStart");

    verify(sound, times(1)).play(0.15f);
  }

  @Test
  void shouldPlayAgainAfterCooldown() {
    when(time.getTime()).thenReturn(0L, 10000L);

    enemy.getEvents().trigger("chaseStart");
    enemy.getEvents().trigger("chaseStart");

    verify(sound, times(2)).play(0.15f);
  }

  @Test
  void shouldNotPlayWhenSoundIsNotLoaded() {
    when(time.getTime()).thenReturn(0L);
    when(resources.containsAsset("sounds/osprey.ogg", Sound.class)).thenReturn(false);

    enemy.getEvents().trigger("chaseStart");

    verify(sound, never()).play(anyFloat());
  }
}
