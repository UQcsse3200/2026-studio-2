package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EnemyConfig;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import java.util.ArrayList;
import java.util.Map;

public class LevelConfig {
  protected PlatformConfig[] platforms;
  protected MovingPlatformConfig[] movingPlatforms;
  protected CrumblingPlatformConfig[] crumblingPlatforms;
  protected TriggerablePlatformConfig[] triggerablePlatforms;
  protected PlatformConfig[] ledges;
  protected SpikeClusterConfig[] spikes;
  protected PlatformConfig[] bounds;
  protected PlatformConfig[] floors;
  protected Map<GridPoint2, EnemyConfig> enemies;
  protected Map<GridPoint2, Item> items;

  protected GridPoint2 playerSpawn;
  protected GridPoint2 winConditionSpawn;

  protected ArrayList<SpawnData> entities = new ArrayList<>();

  /**
   * Creates all entities as specified by the config file and returns the created entities in a Map
   * for the level to spawn the objects at
   *
   * @return A list of SpawnData objects that contain an entity and a coordinate position to spawn
   *     that entity in
   */
  public ArrayList<SpawnData> createEntities() {
    entities = new ArrayList<>();

    createFloors();
    createPlatforms();
    createMovingPlatforms();
    createCrumblingPlatforms();
    createTriggerablePlatforms();
    createLedges();
    createSpikes();
    createWinCondition();
    createItems();

    return entities;
  }

  /**
   * Public getter for the player spawn for a level config
   *
   * @return The spawn coordinates for the player stored by this config as a GridPoint2
   */
  public GridPoint2 getPlayerSpawn() {
    return playerSpawn;
  }

  /**
   * Creates all platforms for this level and adds them to the entities Map for the level to spawn
   */
  private void createPlatforms() {
    if (platforms == null) {
      return;
    }

    for (PlatformConfig p : platforms) {
      Entity platform = ObstacleFactory.createPlatform(p.grappleSides);
      platform.setScale(p.width, p.height);
      entities.add(new SpawnData(p.position, platform));
    }
  }

  /**
   * Creates all moving platforms for this level and adds them to the entities Map for the level to
   * spawn
   */
  private void createMovingPlatforms() {
    if (movingPlatforms == null) {
      return;
    }

    for (MovingPlatformConfig p : movingPlatforms) {
      Entity platform =
          ObstacleFactory.createMovingPlatform(
              p.grappleSides, p.firstTarget, p.secondTarget, p.speed);
      platform.setScale(p.width, p.height);
      entities.add(new SpawnData(p.position, platform));
    }
  }

  /**
   * Creates all crumbling platforms for this level and adds them to the entities Map for the level
   * to spawn
   */
  private void createCrumblingPlatforms() {
    if (crumblingPlatforms == null) {
      return;
    }
  }

  /**
   * Creates all triggerable platforms and buttons associated with those platforms for this level
   * and adds them to the entities Map for the level to spawn
   */
  private void createTriggerablePlatforms() {
    if (triggerablePlatforms == null) {
      return;
    }
  }

  private void createLedges() {
    if (ledges == null) {
      return;
    }

    for (PlatformConfig p : ledges) {
      Entity ledge = ObstacleFactory.createLedge();
      ledge.setScale(p.width, p.height);
      entities.add(new SpawnData(p.position, ledge));
    }
  }

  /**
   * Creates the bounds and floors for the level and adds them to the entities Map for the level to
   * spawn
   */
  private void createFloors() {
    if (bounds != null) {
      for (PlatformConfig b : bounds) {
        Entity bound = ObstacleFactory.createFloor(b.grappleSides);
        bound.setScale(b.width, b.height);
        entities.add(new SpawnData(b.position, bound));
      }
    }

    if (floors != null) {
      for (PlatformConfig f : floors) {
        Entity bound = ObstacleFactory.createFloor(f.grappleSides);
        bound.setScale(f.width, f.height);
        entities.add(new SpawnData(f.position, bound));
      }
    }
  }

  /** Creates all the spike clusters and adds them to the entities map for the level to spawn */
  private void createSpikes() {
    if (spikes == null) {
      return;
    }

    for (SpikeClusterConfig s : spikes) {
      for (int i = s.xMin; i <= s.xMax; i++) {
        for (int j = s.yMin; j <= s.yMax; j++) {
          Entity spike = ObstacleFactory.createSpike();
          entities.add(new SpawnData(new GridPoint2(i, j), spike));
        }
      }
    }
  }

  /** Creates the win condition entity and adds it to the entities Map for the level to spawn */
  private void createWinCondition() {
    if (winConditionSpawn == null) {
      return;
    }

    Entity winCon = ObstacleFactory.createWinConEntity();
    entities.add(new SpawnData(winConditionSpawn, winCon));
  }

  /**
   * Creates all items specified for the level and adds them to the entity tracker when requested
   */
  private void createItems() {
    if (items == null) {
      return;
    }

    for (Map.Entry<GridPoint2, Item> i : items.entrySet()) {
      Entity item = ItemFactory.createItem(i.getValue());
      entities.add(new SpawnData(i.getKey(), item));
    }
  }
}
