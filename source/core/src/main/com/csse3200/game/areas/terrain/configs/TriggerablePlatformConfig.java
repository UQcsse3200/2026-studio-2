package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;

public class TriggerablePlatformConfig extends PlatformConfig {
  String[] ids;
  boolean initialState;

  public TriggerablePlatformConfig(
      GridPoint2 position,
      int width,
      int height,
      int grappleSides,
      String textureFilepath,
      String[] ids,
      boolean initialState) {
    super(position, width, height, grappleSides, textureFilepath);
    this.ids = ids;
    this.initialState = initialState;
  }

  public String[] getIds() {
    return ids;
  }

  public boolean getInitialState() {
    return initialState;
  }
}
