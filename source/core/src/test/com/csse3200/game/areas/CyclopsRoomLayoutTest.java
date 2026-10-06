package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CyclopsRoomLayoutTest {
  private static final int SPACING =
      CyclopsMinigameArea.MAP_SIZE.x / CyclopsMinigameArea.NUM_STATUES;

  @Test
  void statuesAreEvenlySpaced() {
    for (int i = 1; i < CyclopsMinigameArea.NUM_STATUES; i++) {
      assertEquals(
          SPACING, CyclopsMinigameArea.statueTileX(i + 1) - CyclopsMinigameArea.statueTileX(i));
    }
  }

  @Test
  void spacingMatchesOriginalThreeStatueRoom() {
    assertEquals(13, SPACING);
  }

  @Test
  void gapSitsBetweenEachStatueAndTheNext() {
    for (int i = 1; i <= CyclopsMinigameArea.NUM_STATUES; i++) {
      int statue = CyclopsMinigameArea.statueTileX(i);
      int gap = CyclopsMinigameArea.gapTileX(i);
      assertTrue(gap > statue);
      if (i < CyclopsMinigameArea.NUM_STATUES) {
        assertTrue(gap < CyclopsMinigameArea.statueTileX(i + 1));
      }
    }
  }

  @Test
  void statuesAndGapsStayInsideRoom() {
    assertTrue(CyclopsMinigameArea.statueTileX(1) > 0);
    assertTrue(
        CyclopsMinigameArea.gapTileX(CyclopsMinigameArea.NUM_STATUES)
            < CyclopsMinigameArea.MAP_SIZE.x);
  }

  @Test
  void winLocationIsJustPastLastStatue() {
    int lastStatue = CyclopsMinigameArea.statueTileX(CyclopsMinigameArea.NUM_STATUES);
    int win = CyclopsMinigameArea.winTileX();
    assertTrue(win > lastStatue);
    assertTrue(win - lastStatue <= SPACING);
    assertTrue(win < CyclopsMinigameArea.MAP_SIZE.x);
  }
}
