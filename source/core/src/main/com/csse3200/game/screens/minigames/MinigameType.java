package com.csse3200.game.screens.minigames;

/** The minigames available. Used by the select menu to list them. */
public enum MinigameType {
  SPIN_THE_WHEEL("Spin the wheel"),
  CYCLOPS_TIMING("Cyclops Timing Game");

  private final String displayName;

  MinigameType(String displayName) {
    this.displayName = displayName;
  }

  /**
   * @return the human readable name shown on the minigame select menu
   */
  public String getDisplayName() {
    return displayName;
  }
}
