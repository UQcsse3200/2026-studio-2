package com.csse3200.game.components.minigames.cyclopsMinigame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.areas.CyclopsMinigameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

class CyclopsHurtSoundComponentTest {
  private static final float[] VOLUMES_BY_HEARTS = {0.6f, 0.3f, 0.3f, 0.3f, 0.3f};

  private static Sound[] mockVoices() {
    Sound[] voices = new Sound[VOLUMES_BY_HEARTS.length];
    for (int i = 0; i < voices.length; i++) {
      voices[i] = mock(Sound.class);
    }
    return voices;
  }

  private static int totalPlays(Sound[] voices) {
    int plays = 0;
    for (Sound voice : voices) {
      plays += mockingDetails(voice).getInvocations().size();
    }
    return plays;
  }

  @Test
  void heartLevelsMapToTheExpectedFileAndVolume() {
    assertEquals("sounds/hurt_player_1.wav", CyclopsMinigameArea.HURT_VOICE_PATH_BY_HEARTS[4]);
    assertEquals(0.3f, CyclopsMinigameArea.HURT_VOLUME_BY_HEARTS[4]);
    assertEquals("sounds/hurt_player_1.wav", CyclopsMinigameArea.HURT_VOICE_PATH_BY_HEARTS[3]);
    assertEquals(0.3f, CyclopsMinigameArea.HURT_VOLUME_BY_HEARTS[3]);
    assertEquals("sounds/hurt_player_2.wav", CyclopsMinigameArea.HURT_VOICE_PATH_BY_HEARTS[2]);
    assertEquals(0.3f, CyclopsMinigameArea.HURT_VOLUME_BY_HEARTS[2]);
    assertEquals("sounds/hurt_player_2.wav", CyclopsMinigameArea.HURT_VOICE_PATH_BY_HEARTS[1]);
    assertEquals(0.3f, CyclopsMinigameArea.HURT_VOLUME_BY_HEARTS[1]);
  }

  @Test
  void eachHeartLevelPlaysItsVoiceAndVolumeOncePerHurt() {
    // Health after the hit, and the voice index that health maps to (health / 2, capped at 4).
    int[][] healthAndIndex = {{8, 4}, {6, 3}, {4, 2}, {2, 1}, {0, 0}};

    for (int[] pair : healthAndIndex) {
      Sound[] voices = mockVoices();
      Entity player = new Entity().addComponent(new CombatStatsComponent(pair[0], 1));
      CyclopsHurtSoundComponent component =
          new CyclopsHurtSoundComponent(voices, VOLUMES_BY_HEARTS);
      player.addComponent(component);
      component.create();

      player.getEvents().trigger(CyclopsHurtSoundComponent.HURT_EVENT);

      verify(voices[pair[1]]).play(VOLUMES_BY_HEARTS[pair[1]]);
      assertEquals(1, totalPlays(voices));
    }
  }
}
