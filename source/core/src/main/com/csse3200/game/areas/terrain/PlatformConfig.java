package com.csse3200.game.areas.terrain;

import com.badlogic.gdx.math.GridPoint2;

public class PlatformConfig {

  public final GridPoint2 position;
  public final int width;
  public final int height;
  public final int grappleSides;

  public PlatformConfig(GridPoint2 position, int width, int height, int grappleSides) {
    this.position = position;
    this.width = width;
    this.height = height;
    this.grappleSides = grappleSides;
  }
}
