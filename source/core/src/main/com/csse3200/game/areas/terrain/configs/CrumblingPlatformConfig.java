package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;

public class CrumblingPlatformConfig extends PlatformConfig {
  float timeBeforeCrumble; // how long the player can stand on it before crumbling
  float crumbleTime; // how long the crumble takes

  public CrumblingPlatformConfig(
      GridPoint2 position,
      int width,
      int height,
      int grappleSides,
      String textureFilepath,
      float timeBeforeCrumble,
      float crumbleTime) {
    super(position, width, height, grappleSides, textureFilepath);
    this.timeBeforeCrumble = timeBeforeCrumble;
    this.crumbleTime = crumbleTime;
  }
}
