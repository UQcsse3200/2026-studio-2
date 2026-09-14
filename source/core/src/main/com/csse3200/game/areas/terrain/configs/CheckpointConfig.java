package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.entities.Entity;

public class CheckpointConfig {

  private GridPoint2 position;
  private Entity checkpoint;

  public CheckpointConfig(GridPoint2 position) {
    this.position = position;
  }

  public GridPoint2 getPosition() {
    return this.position;
  }

  public Entity getEntity() {
    return this.checkpoint;
  }

  public void setEntity(Entity entity) {
    this.checkpoint = entity;
  }
}
