package com.csse3200.game.areas.terrain.configs;

public class CrumblingPlatformConfig extends PlatformConfig {

  float timeBeforeCrumble;
  float crumbleTime;
  float respawnTime;

  public CrumblingPlatformConfig(
      PlatformConfig platform, float timeBeforeCrumble, float crumbleTime, float respawnTime) {

    super(
        platform.position,
        platform.width,
        platform.height,
        platform.grappleSides,
        platform.textureFilepath);

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
