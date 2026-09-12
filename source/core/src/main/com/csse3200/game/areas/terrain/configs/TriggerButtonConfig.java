package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;

public class TriggerButtonConfig {
  GridPoint2 position;
  float rotation;
  String id;

  public TriggerButtonConfig(GridPoint2 position, float rotation, String id) {
    this.position = position;
    this.rotation = rotation;
    this.id = id;
  }
}
