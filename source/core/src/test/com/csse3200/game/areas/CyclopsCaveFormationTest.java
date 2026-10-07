package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CyclopsCaveFormationTest {
  @Test
  void sixSpotsCycleThroughFormationTexturesInOrder() {
    String[] expected = {
      CyclopsMinigameArea.CAVE_FORMATION_1,
      CyclopsMinigameArea.CAVE_FORMATION_2,
      CyclopsMinigameArea.CAVE_FORMATION_3,
      CyclopsMinigameArea.CAVE_FORMATION_1,
      CyclopsMinigameArea.CAVE_FORMATION_2,
      CyclopsMinigameArea.CAVE_FORMATION_3
    };
    assertEquals(CyclopsMinigameArea.NUM_STATUES, expected.length);
    for (int spot = 1; spot <= CyclopsMinigameArea.NUM_STATUES; spot++) {
      assertEquals(expected[spot - 1], CyclopsMinigameArea.caveFormationTexture(spot));
    }
  }

  @Test
  void formationIsOneAndSevenTenthsTimesPlayerHeight() {
    float playerHeight = 2.84f;
    assertEquals(1.7f * playerHeight, CyclopsMinigameArea.formationHeightFor(playerHeight), 1e-4f);
  }

  @Test
  void formationIsAtLeastOneAndAHalfTimesPlayerHeight() {
    for (float playerHeight : new float[] {1f, 2.84f, 4f}) {
      assertTrue(CyclopsMinigameArea.formationHeightFor(playerHeight) >= 1.5f * playerHeight);
    }
  }
}
