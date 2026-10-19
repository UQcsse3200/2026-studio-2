package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.csse3200.game.areas.CyclopsMinigameArea;
import com.csse3200.game.areas.Level1GameArea;
import com.csse3200.game.components.minigames.blackjack.BlackjackConfig;
import com.csse3200.game.components.minigames.spinthewheel.WheelConfig;
import com.csse3200.game.extensions.GameExtension;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PublicAssetCollectionsTest {
  private static void assertCannotChangeEntries(Object configuration) {
    Collection<?> entries = assertInstanceOf(Collection.class, configuration);
    assertThrows(UnsupportedOperationException.class, entries::clear);
    assertThrows(UnsupportedOperationException.class, () -> entries.add(null));
    if (entries instanceof List<?> ordered) {
      assertThrows(UnsupportedOperationException.class, () -> ordered.set(0, null));
    }
  }

  @Test
  void callersCannotChangeCyclopsHurtVoices() {
    assertCannotChangeEntries(CyclopsMinigameArea.HURT_VOICE_PATH_BY_HEARTS);
  }

  @Test
  void callersCannotChangeCyclopsHurtVolumes() {
    assertCannotChangeEntries(CyclopsMinigameArea.HURT_VOLUME_BY_HEARTS);
  }

  @Test
  void callersCannotReplaceGoldSpawnLocations() {
    assertCannotChangeEntries(Level1GameArea.GOLD_SPAWNS);
  }

  @Test
  void callersCannotChangeButtonSounds() {
    assertCannotChangeEntries(ButtonSound.SOUND_FILES);
  }

  @Test
  void callersCannotChangeSharedSoundEffects() {
    assertCannotChangeEntries(SoundEffects.FILES);
  }

  @Test
  void callersCannotChangeBlackjackTextures() {
    assertCannotChangeEntries(BlackjackConfig.TEXTURES);
  }

  @Test
  void callersCannotChangeBlackjackSounds() {
    assertCannotChangeEntries(BlackjackConfig.SOUNDS);
  }

  @Test
  void callersCannotChangeWheelTextures() {
    assertCannotChangeEntries(WheelConfig.TEXTURES);
  }

  @Test
  void callersCannotChangeWheelSounds() {
    assertCannotChangeEntries(WheelConfig.SOUNDS);
  }
}
