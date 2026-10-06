package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;

public class SlipperyPlatformConfig extends PlatformConfig {
  float maxGrappleTime; // how long a grapple can be attached to this platform
  float slipperiness; // the friction of the platform

  public SlipperyPlatformConfig(
      GridPoint2 position,
      int width,
      int height,
      int grappleSides,
      String textureFilepath,
      float maxGrappleTime,
      float slipperiness) {
    super(position, width, height, grappleSides, textureFilepath);
    this.maxGrappleTime = maxGrappleTime;
    this.slipperiness = slipperiness;
  }

  public float getMaxGrappleTime() {
    return maxGrappleTime;
  }

  public float getSlipperiness() {
    return slipperiness;
  }
}
