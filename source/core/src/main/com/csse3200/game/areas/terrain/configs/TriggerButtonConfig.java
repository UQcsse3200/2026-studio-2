package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;

public class TriggerButtonConfig {
  GridPoint2 position;
  float rotation;
  boolean attached;
  String[] ids;

  public TriggerButtonConfig(GridPoint2 position, float rotation, boolean attached, String[] ids) {
    this.position = position;
    this.rotation = rotation;
    this.attached = attached;
    this.ids = ids;
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

  public String[] getIds() {
    return ids;
  }
}
