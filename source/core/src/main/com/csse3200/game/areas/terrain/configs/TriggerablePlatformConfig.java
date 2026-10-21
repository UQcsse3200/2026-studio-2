package com.csse3200.game.areas.terrain.configs;

public class TriggerablePlatformConfig extends PlatformConfig {
  String[] ids;
  boolean initialState;
  boolean singleActivation;

  public TriggerablePlatformConfig(
      PlatformConfig platform, String[] ids, boolean initialState, boolean singleActivation) {
    super(
        platform.position,
        platform.width,
        platform.height,
        platform.grappleSides,
        platform.textureFilepath);
    this.ids = ids;
    this.initialState = initialState;
    this.singleActivation = singleActivation;
  }

  public String[] getIds() {
    return ids;
  }

  public boolean getInitialState() {
    return initialState;
  }

  public boolean getSingleActivation() {
    return singleActivation;
  }
}
