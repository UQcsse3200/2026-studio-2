package com.csse3200.game.entities.configs;

/** Available character portraits that can be displayed in a text box. */
public enum Portrait {
  CALYPSO_HAPPY("images/portraits/calypsoHappy.png"),
  CALYPSO_FRUSTRATED("images/portraits/calypsoFrustrated.png"),
  CALYPSO_REGRET("images/portraits/calypsoRegret.png");

  private final String assetPath;

  Portrait(String assetPath) {
    this.assetPath = assetPath;
  }

  /** Returns the path to this portrait relative to the assets directory. */
  public String getAssetPath() {
    return assetPath;
  }
}
