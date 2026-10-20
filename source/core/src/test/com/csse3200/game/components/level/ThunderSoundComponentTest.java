package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyFloat;
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
class ThunderSoundComponentTest {
  private Sound close;
  private Sound far;
  private GameTime time;
  private ThunderSoundComponent thunder;

  @BeforeEach
  void setUp() {
    ResourceService resources = mock(ResourceService.class);
    close = mock(Sound.class);
    far = mock(Sound.class);
    when(resources.containsAsset(ThunderSoundComponent.CLOSE_THUNDER, Sound.class))
        .thenReturn(true);
    when(resources.getAsset(ThunderSoundComponent.CLOSE_THUNDER, Sound.class)).thenReturn(close);
    when(resources.containsAsset(ThunderSoundComponent.FAR_THUNDER, Sound.class)).thenReturn(true);
    when(resources.getAsset(ThunderSoundComponent.FAR_THUNDER, Sound.class)).thenReturn(far);
    ServiceLocator.registerResourceService(resources);
    time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    thunder = new ThunderSoundComponent();
    new Entity().addComponent(thunder).create();
  }

  private void advance(float seconds, float step) {
    when(time.getDeltaTime()).thenReturn(step);
    for (float t = 0f; t < seconds - 0.0001f; t += step) {
      thunder.update();
    }
  }

  @Test
  void shouldBeSilentBeforeTheFirstStrike() {
    advance(4.5f, 0.5f);

    verify(close, never()).play(anyFloat());
    verify(far, never()).play(anyFloat());
  }

  @Test
  void shouldPlayCloseThunderAtFiveSecondsAndFarAtThirteen() {
    advance(5f, 0.5f);
    verify(close, times(1)).play(ThunderSoundComponent.CLOSE_VOLUME);
    verify(far, never()).play(anyFloat());

    advance(8f, 0.5f);
    verify(far, times(1)).play(ThunderSoundComponent.FAR_VOLUME);
  }

  @Test
  void shouldPlayEachThunderOncePerCycle() {
    advance(40f, 0.5f);

    verify(close, times(2)).play(anyFloat());
    verify(far, times(2)).play(anyFloat());
  }

  @Test
  void shouldDetectCrossingsIncludingTheCycleWrap() {
    assertTrue(ThunderSoundComponent.crossed(4.9f, 5.1f, 5f));
    assertFalse(ThunderSoundComponent.crossed(5.1f, 5.3f, 5f));
    assertFalse(ThunderSoundComponent.crossed(19.9f, 0.1f, 5f));
    assertTrue(ThunderSoundComponent.crossed(19.9f, 5.2f, 5f));
  }

  @Test
  void shouldStopBothThundersOnPause() {
    ThunderSoundComponent.stopAll();

    verify(close).stop();
    verify(far).stop();
  }

  @Test
  void shouldStopBothThundersWhenDisposed() {
    thunder.dispose();

    verify(close).stop();
    verify(far).stop();
  }

  @Test
  void shouldDoNothingWithoutATimeSource() {
    ServiceLocator.registerTimeSource(null);

    thunder.update();

    verify(close, never()).play(anyFloat());
    verify(far, never()).play(anyFloat());
  }

  @Test
  void shouldNotCrashStoppingWithoutAResourceService() {
    ServiceLocator.registerResourceService(null);

    org.junit.jupiter.api.Assertions.assertDoesNotThrow(ThunderSoundComponent::stopAll);
  }
}
