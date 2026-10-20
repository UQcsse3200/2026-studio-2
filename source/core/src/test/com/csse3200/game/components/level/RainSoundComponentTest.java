package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.extensions.GameExtension;
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
}
