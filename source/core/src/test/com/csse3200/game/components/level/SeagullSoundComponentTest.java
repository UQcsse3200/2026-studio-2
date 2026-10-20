package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class SeagullSoundComponentTest {
  private ResourceService resources;
  private Sound sound;
  private GameTime time;

  @BeforeEach
  void setUp() {
    resources = mock(ResourceService.class);
    sound = mock(Sound.class);
    when(resources.containsAsset(anyString(), eq(Sound.class))).thenReturn(true);
    when(resources.getAsset(anyString(), eq(Sound.class))).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);
    time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
  }

  private SeagullSoundComponent createSeagulls(int maxCalls) {
    SeagullSoundComponent seagulls = new SeagullSoundComponent(maxCalls);
    new Entity().addComponent(seagulls).create();
    return seagulls;
  }

  @Test
  void shouldNotPlayBeforeTheShortestDelay() {
    SeagullSoundComponent seagulls = createSeagulls(3);
    when(time.getDeltaTime()).thenReturn(SeagullSoundComponent.FIRST_MIN_DELAY - 1f);

    seagulls.update();

    verify(sound, never()).play(anyFloat());
  }

  @Test
  void shouldPlayAQuietClipOnceTheDelayHasPassed() {
    SeagullSoundComponent seagulls = createSeagulls(3);
    when(time.getDeltaTime()).thenReturn(SeagullSoundComponent.MAX_DELAY);

    seagulls.update();

    verify(sound).play(SeagullSoundComponent.VOLUME);
    assertEquals(1, seagulls.getCalls());
  }

  @Test
  void shouldNeverPlayMoreThanTheCallLimit() {
    SeagullSoundComponent seagulls = createSeagulls(2);
    when(time.getDeltaTime()).thenReturn(SeagullSoundComponent.MAX_DELAY);

    for (int i = 0; i < 10; i++) {
      seagulls.update();
    }

    verify(sound, times(2)).play(anyFloat());
    assertEquals(2, seagulls.getCalls());
  }

  @Test
  void shouldStopClipsWhenDisposed() {
    SeagullSoundComponent seagulls = createSeagulls(3);

    seagulls.dispose();

    verify(sound, times(SeagullSoundComponent.CLIPS.size())).stop();
  }

  @Test
  void shouldNotPlayWhenClipIsNotLoaded() {
    when(resources.containsAsset(anyString(), eq(Sound.class))).thenReturn(false);
    SeagullSoundComponent seagulls = createSeagulls(3);
    when(time.getDeltaTime()).thenReturn(SeagullSoundComponent.MAX_DELAY);

    seagulls.update();

    verify(sound, never()).play(anyFloat());
  }
}
