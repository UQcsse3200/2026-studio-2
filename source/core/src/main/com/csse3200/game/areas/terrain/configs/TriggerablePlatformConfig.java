package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;

public class TriggerablePlatformConfig extends PlatformConfig {
  String[] ids;
  boolean initialState;
  boolean singleActivation;

  public TriggerablePlatformConfig(
      GridPoint2 position,
      int width,
      int height,
      int grappleSides,
      String textureFilepath,
      String[] ids,
      boolean initialState,
      boolean singleActivation) {
    super(position, width, height, grappleSides, textureFilepath);
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
