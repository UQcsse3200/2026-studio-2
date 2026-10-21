package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ButtonSoundTest {

  @Test
  void shouldPlayTheClickAtItsVolume() {
    ResourceService resources = mock(ResourceService.class);
    Sound sound = mock(Sound.class);
    when(resources.getAsset("sounds/Button_Sound_02.wav", Sound.class)).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);

    ButtonSound.playClick();

    verify(sound).play(0.7f);
  }

  @Test
  void shouldNotCrashWhenTheClickIsMissing() {
    ResourceService resources = mock(ResourceService.class);
    ServiceLocator.registerResourceService(resources);

    assertDoesNotThrow(ButtonSound::playClick);
  }

  @Test
  void shouldNotCrashWhenTheClickFailsToLoad() {
    ResourceService resources = mock(ResourceService.class);
    when(resources.getAsset(anyString(), eq(Sound.class)))
        .thenThrow(new GdxRuntimeException("not loaded"));
    ServiceLocator.registerResourceService(resources);

    assertDoesNotThrow(ButtonSound::playClick);
  }

  @Test
  void shouldNotCrashWithoutAResourceService() {
    assertDoesNotThrow(ButtonSound::playClick);
  }

  @Test
  void shouldLoadAndUnloadTheClickSound() {
    ResourceService resources = mock(ResourceService.class);

    ButtonSound.load(resources);
    ButtonSound.unload(resources);

    verify(resources).loadSounds(ButtonSound.SOUND_FILES.toArray(String[]::new));
    verify(resources).unloadAssets(ButtonSound.SOUND_FILES.toArray(String[]::new));
  }

  @Test
  void shouldPlayTheClickBeforeRunningTheAction() {
    ResourceService resources = mock(ResourceService.class);
    Sound sound = mock(Sound.class);
    when(resources.getAsset("sounds/Button_Sound_02.wav", Sound.class)).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);
    AtomicBoolean ran = new AtomicBoolean(false);

    ButtonSound.playClickThen(() -> ran.set(true));

    verify(sound).play(0.7f);
    assertFalse(ran.get());
  }
}
