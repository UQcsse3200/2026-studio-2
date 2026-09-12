package com.csse3200.game.screens.minigames;

/** The minigames available. Used by the select menu to list them. */
public enum MinigameType {
  SPIN_THE_WHEEL("Spin the wheel", "spinTheWheel"),
  BLACKJACK("Blackjack", "blackJack"),
  CYCLOPS_TIMING("Cyclops Timing Game", "cyclops");

  private final String displayName;
  private final String assetKey;

  MinigameType(String displayName, String assetKey) {
    this.displayName = displayName;
    this.assetKey = assetKey;
  }

  /**
   * @return the human readable name shown on the minigame select menu
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @return the filename prefix used for this minigame's select-menu button art (e.g.
   *     "images/Buttons/&lt;assetKey&gt;_up_btn.png")
   */
  public String getAssetKey() {
    return assetKey;
  }
}
