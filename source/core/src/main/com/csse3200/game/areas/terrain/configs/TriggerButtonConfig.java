package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;

public class TriggerButtonConfig {
  GridPoint2 position;
  float rotation;
  boolean attached;
  String id;

  public TriggerButtonConfig(GridPoint2 position, float rotation, boolean attached, String id) {
    this.position = position;
    this.rotation = rotation;
    this.attached = attached;
    this.id = id;
  }

  public GridPoint2 getPosition() {
    return position;
  }

  public float getRotation() {
    return rotation;
  }

  public boolean getAttached() {
    return attached;
  }

  public String getId() {
    return id;
  }
}
