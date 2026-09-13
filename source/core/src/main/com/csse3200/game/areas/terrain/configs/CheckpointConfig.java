package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;

public class CheckpointConfig {
  private GridPoint2 position;

  public CheckpointConfig(GridPoint2 position) {
    this.position = position;
  }

  public GridPoint2 getPosition() {
    return this.position;
  }
}
