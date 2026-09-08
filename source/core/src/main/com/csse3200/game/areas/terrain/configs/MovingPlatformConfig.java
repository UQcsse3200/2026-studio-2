package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;

public class MovingPlatformConfig extends PlatformConfig {
  public Vector2 firstTarget;
  public Vector2 secondTarget;
  public Vector2 speed;

  public MovingPlatformConfig(
      GridPoint2 position,
      int width,
      int height,
      int grappleSides,
      Vector2 firstTarget,
      Vector2 secondTarget,
      Vector2 speed) {
    super(position, width, height, grappleSides);
    this.firstTarget = firstTarget;
    this.secondTarget = secondTarget;
    this.speed = speed;
  }
}
