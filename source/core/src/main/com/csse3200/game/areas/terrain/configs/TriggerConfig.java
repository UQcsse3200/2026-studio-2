package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;

public class TriggerConfig {
  public GridPoint2 position;
  public Vector2 scale;
  public String[] ids;
  public boolean oneTimeActivation;

  public TriggerConfig(
      GridPoint2 position, Vector2 scale, String[] ids, boolean oneTimeActivation) {
    this.position = position;
    this.scale = scale;
    this.ids = ids;
    this.oneTimeActivation = oneTimeActivation;
  }
}
