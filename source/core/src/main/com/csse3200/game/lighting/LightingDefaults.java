package com.csse3200.game.lighting;

import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.physics.PhysicsLayer;

public final class LightingDefaults {
  private LightingDefaults() {} // do not initialise

  // Lighting Engine Defaults
  public static final float AMBIENT_LIGHT = 0.9f;
  public static final int BLUR_NUM = 3;

  // Light Defaults
  public static final int RAYS = 128;
  public static final float DIST = 4f;

  // Colours
  public static final Color NORMAL_COLOR =
      new Color(230f / 255f, 210f / 255f, 140f / 255f, 70f / 100f);
  // Security Camera
  public static final short OCCLUDER = PhysicsLayer.OBSTACLE;
}