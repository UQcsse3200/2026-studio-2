package com.csse3200.game.lighting;

/** Default lighting parameters shared by game areas. */
public final class LightingDefaults {
  private LightingDefaults() {}

  /** Number of passes used for light blur. */
  public static final int BLUR_NUM = 3;

  /** Default number of shadow rays for game lights. */
  public static final int RAYS = 128;

  /** Default light radius in world units. */
  public static final float DIST = 4f;
}
