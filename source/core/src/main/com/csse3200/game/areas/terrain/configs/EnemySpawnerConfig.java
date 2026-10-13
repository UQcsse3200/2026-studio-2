package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;

public class EnemySpawnerConfig {
  public final GridPoint2 position;
  public SpawnerConfig spawnData;
  public String[] ids; // any ids that should trigger the activation of this spawner
  public String[] completionIds; // any ids to trigger when this spawner has been defeated

  /**
   * Constructor that allows for a custom spawn coordinate position in the world. If used, the spawn
   * position needs to be updated when the entity's position is updated in the config file
   *
   * @param position where to spawn this entity in the world
   * @param spawnData a SpawnerConfig that outlines what can be spawned, how often, etc.
   * @param ids what strings should trigger this entity's activation component
   * @param completionIds what strings should be triggered by this spawner upon the player beating
   *     all enemies spawned
   */
  public EnemySpawnerConfig(
      GridPoint2 position, SpawnerConfig spawnData, String[] ids, String[] completionIds) {
    this.position = position;
    this.spawnData = spawnData;
    this.ids = ids;
    this.completionIds = completionIds;
  }
}
