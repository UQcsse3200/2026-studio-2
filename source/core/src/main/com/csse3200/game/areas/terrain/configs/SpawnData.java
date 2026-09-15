package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.entities.Entity;

public class SpawnData {
  public GridPoint2 pos;
  public Entity entity;

  /**
   * Creates new SpawnData object used to spawn an entity in a position in a level
   *
   * @param pos Position to spawn the entity at
   * @param entity Entity to spawn at specified position
   */
  public SpawnData(GridPoint2 pos, Entity entity) {
    this.pos = pos;
    this.entity = entity;
  }
}
