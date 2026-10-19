package com.csse3200.game.screens.minigames;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MinigameTypeTest {
  @Test
  void exposesMenuLabelsAndAssetPrefixes() {
    assertEquals("Spin the wheel", MinigameType.SPIN_THE_WHEEL.getDisplayName());
    assertEquals("spinTheWheel", MinigameType.SPIN_THE_WHEEL.getAssetKey());
    assertEquals("Blackjack", MinigameType.BLACKJACK.getDisplayName());
    assertEquals("blackJack", MinigameType.BLACKJACK.getAssetKey());
    assertEquals("Cyclops Timing Game", MinigameType.CYCLOPS_TIMING.getDisplayName());
    assertEquals("cyclops", MinigameType.CYCLOPS_TIMING.getAssetKey());
  }
}
