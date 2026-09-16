package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.components.level.SpawnerComponent;

public class SpikyBallTrapConfig {
  GridPoint2 position;
  float rotation;
  String[] ids;
  float spawnInterval;
  boolean initialState;
  SpawnerComponent.ACTIVATION_MODE mode;

  public SpikyBallTrapConfig(
      GridPoint2 position,
      float rotation,
      String[] ids,
      float spawnInterval,
      boolean initialState,
      SpawnerComponent.ACTIVATION_MODE mode) {
    this.position = position;
    this.rotation = rotation;
    this.ids = ids;
    this.spawnInterval = spawnInterval;
    this.initialState = initialState;
    this.mode = mode;
  }

  public GridPoint2 getPosition() {
    return position;
  }

  public float getRotation() {
    return rotation;
  }

  public String[] getIds() {
    return ids;
  }

  public float getSpawnInterval() {
    return spawnInterval;
  }

  public boolean getInitialState() {
    return initialState;
  }

  public SpawnerComponent.ACTIVATION_MODE getMode() {
    return mode;
  }
}
