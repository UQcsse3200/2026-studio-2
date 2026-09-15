package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;

public class CrumblingPlatformConfig extends PlatformConfig {

  float timeBeforeCrumble;
  float crumbleTime;
  float respawnTime;

  public CrumblingPlatformConfig(
      GridPoint2 position,
      int width,
      int height,
      int grappleSides,
      String textureFilepath,
      float timeBeforeCrumble,
      float crumbleTime,
      float respawnTime) {

    super(position, width, height, grappleSides, textureFilepath);

    this.timeBeforeCrumble = timeBeforeCrumble;
    this.crumbleTime = crumbleTime;
    this.respawnTime = respawnTime;
  }

  public float getTimeBeforeCrumble() {
    return timeBeforeCrumble;
  }

  public float getCrumbleTime() {
    return crumbleTime;
  }

  public float getRespawnTime() {
    return respawnTime;
  }
}
