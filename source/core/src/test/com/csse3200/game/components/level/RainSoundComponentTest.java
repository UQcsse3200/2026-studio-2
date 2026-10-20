package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Music;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class RainSoundComponentTest {

  @Test
  void shouldBeQuietestAtTheBottom() {
    assertEquals(RainSoundComponent.MIN_VOLUME, RainSoundComponent.volumeForHeight(0f), 0.0001f);
  }

  @Test
  void shouldGetLouderTheHigherThePlayerClimbs() {
    float low = RainSoundComponent.volumeForHeight(20f);
    float middle = RainSoundComponent.volumeForHeight(50f);
    float high = RainSoundComponent.volumeForHeight(90f);

    assertTrue(low < middle);
    assertTrue(middle < high);
  }

  @Test
  void shouldBeLoudestAtTheTop() {
    assertEquals(RainSoundComponent.MAX_VOLUME, RainSoundComponent.volumeForHeight(120f), 0.0001f);
  }

  @Test
  void shouldSetTheRainVolumeFromThePlayersHeightEachFrame() {
    ResourceService resources = mock(ResourceService.class);
    Music rain = mock(Music.class);
    when(resources.containsAsset(RainSoundComponent.RAIN_MUSIC, Music.class)).thenReturn(true);
    when(resources.getAsset(RainSoundComponent.RAIN_MUSIC, Music.class)).thenReturn(rain);
    ServiceLocator.registerResourceService(resources);
    Entity player = new Entity();
    player.setPosition(0f, 120f);
    RainSoundComponent component = new RainSoundComponent(player);
    new Entity().addComponent(component).create();

    component.update();

    verify(rain).setVolume(RainSoundComponent.MAX_VOLUME);
  }

  @Test
  void shouldDoNothingWithoutAPlayer() {
    ResourceService resources = mock(ResourceService.class);
    ServiceLocator.registerResourceService(resources);
    RainSoundComponent component = new RainSoundComponent(null);
    new Entity().addComponent(component).create();

    component.update();

    verify(resources, never()).getAsset(RainSoundComponent.RAIN_MUSIC, Music.class);
  }

  @Test
  void shouldDoNothingWhenTheRainIsNotLoaded() {
    ResourceService resources = mock(ResourceService.class);
    when(resources.containsAsset(RainSoundComponent.RAIN_MUSIC, Music.class)).thenReturn(false);
    ServiceLocator.registerResourceService(resources);
    RainSoundComponent component = new RainSoundComponent(new Entity());
    new Entity().addComponent(component).create();

    component.update();

    verify(resources, never()).getAsset(RainSoundComponent.RAIN_MUSIC, Music.class);
  }
}
