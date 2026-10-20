package com.csse3200.game.areas.terrain.configs;

import com.csse3200.game.components.level.SpawnerComponent;
import com.csse3200.game.entities.Entity;
import java.util.List;
import java.util.function.Supplier;

public class SpawnerConfig {
  public List<Supplier<Entity>> spawns;
  public float spawnInterval;
  public int maxSpawns;
  public SpawnerComponent.ACTIVATION_MODE mode;
  public boolean active;

  public SpawnerConfig(
      List<Supplier<Entity>> spawns,
      float interval,
      int maxSpawns,
      SpawnerComponent.ACTIVATION_MODE mode,
      boolean active) {
    this.spawns = spawns;
    this.spawnInterval = interval;
    this.maxSpawns = maxSpawns;
    this.mode = mode;
    this.active = active;
  }
}
