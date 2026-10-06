package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class CrumblingPlatformComponentTest {
  private Sound sound;
  private Entity platform;

  @BeforeEach
  void setUp() {
    ResourceService resources = mock(ResourceService.class);
    sound = mock(Sound.class);
    when(resources.containsAsset("sounds/rock_break.ogg", Sound.class)).thenReturn(true);
    when(resources.getAsset("sounds/rock_break.ogg", Sound.class)).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.1f);
    ServiceLocator.registerTimeSource(time);
    platform = new Entity().addComponent(new CrumblingPlatformComponent(0, 1f, 1f, 3f));
    platform.create();
  }

  private void touchWithPlayer() {
    Fixture other = mock(Fixture.class);
    Filter filter = new Filter();
    filter.categoryBits = PhysicsLayer.PLAYER;
    when(other.getFilterData()).thenReturn(filter);
    platform.getEvents().trigger("collisionStart", mock(Fixture.class), other);
  }

  private void update(int frames) {
    for (int i = 0; i < frames; i++) {
      platform.update();
    }
  }

  @Test
  void shouldNotPlaySoundBeforePlayerTouches() {
    update(10);

    verify(sound, never()).play(anyFloat());
  }

  @Test
  void shouldPlayCrumbleSoundsThatGetLouder() {
    touchWithPlayer();
    update(15);

    ArgumentCaptor<Float> volumes = ArgumentCaptor.forClass(Float.class);
    verify(sound, atLeast(2)).play(volumes.capture());
    List<Float> played = volumes.getAllValues();
    for (int i = 1; i < played.size(); i++) {
      assertTrue(played.get(i) > played.get(i - 1));
    }
  }
}
