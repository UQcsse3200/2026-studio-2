package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SoundEffectsTest {

  @Test
  void shouldPlayLoadedSoundAtGivenVolume() {
    ResourceService resources = mock(ResourceService.class);
    Sound sound = mock(Sound.class);
    when(resources.containsAsset("sounds/jump.ogg", Sound.class)).thenReturn(true);
    when(resources.getAsset("sounds/jump.ogg", Sound.class)).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);

    SoundEffects.play("sounds/jump.ogg", 0.5f);

    verify(sound).play(0.5f);
  }

  @Test
  void shouldNotPlaySoundThatIsNotLoaded() {
    ResourceService resources = mock(ResourceService.class);
    when(resources.containsAsset("sounds/jump.ogg", Sound.class)).thenReturn(false);
    ServiceLocator.registerResourceService(resources);

    SoundEffects.play("sounds/jump.ogg", 0.5f);

    verify(resources, never()).getAsset(anyString(), eq(Sound.class));
  }

  @Test
  void shouldNotCrashWithoutResourceService() {
    assertDoesNotThrow(() -> SoundEffects.play("sounds/jump.ogg", 0.5f));
  }

  @Test
  void shouldLoadAndUnloadAllSoundFiles() {
    ResourceService resources = mock(ResourceService.class);

    SoundEffects.load(resources);
    SoundEffects.unload(resources);

    verify(resources).loadSounds(SoundEffects.FILES);
    verify(resources).unloadAssets(SoundEffects.FILES);
  }
}
