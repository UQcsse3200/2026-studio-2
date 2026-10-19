package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.badlogic.gdx.audio.Music;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GameVolumeTest {

  @AfterEach
  void resetVolume() {
    GameVolume.set(1f);
  }

  @Test
  void shouldScaleByVolume() {
    GameVolume.set(0.5f);

    assertEquals(0.2f, GameVolume.scale(0.4f), 0.0001f);
  }

  @Test
  void shouldClampVolumeBetweenZeroAndOne() {
    GameVolume.set(2f);
    assertEquals(1f, GameVolume.get(), 0.0001f);

    GameVolume.set(-1f);
    assertEquals(0f, GameVolume.get(), 0.0001f);
  }

  @Test
  void shouldUpdatePlayingMusicWhenVolumeChanges() {
    Music music = mock(Music.class);
    GameVolume.setMusicVolume(music, 0.4f);
    verify(music).setVolume(0.4f);

    GameVolume.set(0.5f);

    verify(music).setVolume(0.2f);
  }

  @Test
  void shouldDefaultToFullVolume() {
    assertEquals(1f, GameVolume.get(), 0.0001f);
  }
}
