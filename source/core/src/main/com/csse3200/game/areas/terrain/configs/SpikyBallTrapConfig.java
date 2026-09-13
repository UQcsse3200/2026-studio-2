package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;

public class SpikyBallTrapConfig {
  GridPoint2 position;
  float rotation;
  String id;
  float spawnInterval;

  public SpikyBallTrapConfig(GridPoint2 position, float rotation, String id, float spawnInterval) {
    this.position = position;
    this.rotation = rotation;
    this.id = id;
    this.spawnInterval = spawnInterval;
  }

  public GridPoint2 getPosition() {
    return position;
  }

  public float getRotation() {
    return rotation;
  }

  public String getId() {
    return id;
  }

  public float getSpawnInterval() {
    return spawnInterval;
  }
}
