package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.Vector2;

public class MovingPlatformConfig extends PlatformConfig {
  public Vector2 firstTarget;
  public Vector2 secondTarget;
  public Vector2 speed;
  public String[] activateIds;

  public MovingPlatformConfig(
      PlatformConfig platform,
      Vector2 firstTarget,
      Vector2 secondTarget,
      Vector2 speed,
      String[] activateIds) {
    super(
        platform.position,
        platform.width,
        platform.height,
        platform.grappleSides,
        platform.textureFilepath);
    this.firstTarget = firstTarget;
    this.secondTarget = secondTarget;
    this.speed = speed;
    this.activateIds = activateIds;
  }

  public Vector2 getFirstTarget() {
    return firstTarget;
  }

  public Vector2 getSecondTarget() {
    return secondTarget;
  }

  public Vector2 getSpeed() {
    return speed;
  }

  public String[] getActivateIds() {
    return activateIds;
  }
}
