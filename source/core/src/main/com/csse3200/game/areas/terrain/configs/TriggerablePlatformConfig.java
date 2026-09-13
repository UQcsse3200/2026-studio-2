package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;

public class TriggerablePlatformConfig extends PlatformConfig {
  String id;

  public TriggerablePlatformConfig(
      GridPoint2 position,
      int width,
      int height,
      int grappleSides,
      String textureFilepath,
      String id) {
    super(position, width, height, grappleSides, textureFilepath);
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
