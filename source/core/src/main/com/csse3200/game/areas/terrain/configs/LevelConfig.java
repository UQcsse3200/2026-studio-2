package com.csse3200.game.areas.terrain.configs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.level.CheckpointComponent;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EnemyConfig;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LevelConfig {
  /** Draw depth of checkpoint statues: behind the player and the level's platforms. */
  private static final float CHECKPOINT_Z_INDEX = -100f;

  protected String platformTFP;
  protected String mossyPlatformTFP;
  protected String movingPlatformTFP;
  protected String crumblingPlatformTFP;
  protected String triggerablePlatformTFP;
  protected String ledgesTFP;
  protected String groundTFP;
  protected String spikeTFP;

  /** Optional atlas with "unlit" and "lit" regions for this level's checkpoints. */
  protected String checkpointAtlas;

  /** World height of this level's checkpoint statues; their width follows the art. */
  protected float checkpointHeight = CheckpointComponent.ATLAS_HEIGHT;

  protected PlatformConfig[] platforms;
  protected MovingPlatformConfig[] movingPlatforms;
  protected CrumblingPlatformConfig[] crumblingPlatforms;
  protected TriggerablePlatformConfig[] triggerablePlatforms;
  protected SlipperyPlatformConfig[] slipperyPlatforms;
  protected PlatformConfig[] ledges;

  protected SpikeClusterConfig[] spikes;
  protected SpikyBallTrapConfig[] ballTraps;
  protected TriggerButtonConfig[] triggerButtons;
  protected PlatformConfig[] bounds;
  protected PlatformConfig[] floors;
  protected Map<GridPoint2, EnemyConfig> enemies;
  protected Map<GridPoint2, Item> items;
  protected CheckpointConfig[] checkpoints;
  protected EnemySpawnerConfig[] enemySpawners;
  protected TriggerConfig[] mapTriggers;

  protected GridPoint2 playerSpawn;
  protected GridPoint2 nextLevelTriggerSpawn;
  protected String nextLevelName;

  /** Height of the portal at the end of the level, so it can be fitted to the platform below. */
  protected float nextLevelPortalHeight = ObstacleFactory.DEFAULT_LEVEL_END_HEIGHT;

  protected GridPoint2 winConditionSpawn;
  protected GridPoint2[] wheelSpinSpawns;

  protected List<SpawnData> entities = new ArrayList<>();

  /**
   * Creates all entities as specified by the config file and returns the created entities in a Map
   * for the level to spawn the objects at
   *
   * @return A list of SpawnData objects that contain an entity and a coordinate position to spawn
   *     that entity in
   */
  public List<SpawnData> createEntities() {
    entities = new ArrayList<>();

    createFloors();
    createPlatforms();
    createMovingPlatforms();
    createCrumblingPlatforms();
    createTriggerablePlatforms();
    createSlipperyPlatforms();
    createLedges();
    createSpikes();
    createTraps();
    createTriggerButtons();
    createWinCondition();
    createLevelTrigger();
    createItems();
    createCheckpoints();
    createSpawners();
    createTriggers();

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
   * Public getter for the name of the level this level transitions to
   *
   * @return the next level's name, or null if this level has no next level
   */
  public String getNextLevelName() {
    return nextLevelName;
  }

  /**
   * Public getter to access checkpoint components for levels
   *
   * @return a list of the CheckpointComponents of a level
   */
  public List<CheckpointComponent> getCheckpoints() {
    List<CheckpointComponent> checkpointComponents = new ArrayList<>();
    if (checkpoints == null) {
      return checkpointComponents;
    }

    java.util.Arrays.stream(checkpoints)
        .map(c -> c.getEntity().getComponent(CheckpointComponent.class))
        .forEach(checkpointComponents::add);

    return checkpointComponents;
  }

  public GridPoint2[] getWheelSpinSpawns() {
    return wheelSpinSpawns;
  }

  /**
   * Creates all platforms for this level and adds them to the entities Map for the level to spawn
   */
  private void createPlatforms() {
    if (platforms == null) {
      return;
    }

    for (PlatformConfig p : platforms) {
      Entity platform = ObstacleFactory.createPlatform(p);
      platform.setScale((float) p.width, (float) p.height);
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
      Entity platform = ObstacleFactory.createMovingPlatform(p);
      platform.setScale((float) p.width, (float) p.height);
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

    for (CrumblingPlatformConfig c : crumblingPlatforms) {
      Entity platform = ObstacleFactory.createCrumblingPlatform(c);
      platform.setScale((float) c.width, (float) c.height);
      entities.add(new SpawnData(c.position, platform));
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

    for (TriggerablePlatformConfig t : triggerablePlatforms) {
      Entity triggerablePlatform = ObstacleFactory.createTriggerablePlatform(t);
      triggerablePlatform.setScale((float) t.width, (float) t.height);
      entities.add(new SpawnData(t.position, triggerablePlatform));
    }
  }

  /**
   * Creates all slippery platforms for this level and adds them to the entities Map for the level
   * to spawn
   */
  private void createSlipperyPlatforms() {
    if (slipperyPlatforms == null) {
      return;
    }

    for (SlipperyPlatformConfig s : slipperyPlatforms) {
      Entity slipperyPlatform = ObstacleFactory.createSlipperyPlatform(s);
      slipperyPlatform.setScale((float) s.width, (float) s.height);
      entities.add(new SpawnData(s.position, slipperyPlatform));
    }
  }

  private void createLedges() {
    if (ledges == null) {
      return;
    }

    for (PlatformConfig p : ledges) {
      // A ledge that uses the ground tile repeats it, as floors do, instead of stretching it.
      boolean tiled = p.textureFilepath != null && p.textureFilepath.equals(groundTFP);
      Entity ledge = ObstacleFactory.createLedge(p, tiled);
      ledge.setScale((float) p.width, (float) p.height);
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
        Entity bound = ObstacleFactory.createFloor(b);
        bound.setScale((float) b.width, (float) b.height);
        entities.add(new SpawnData(b.position, bound));
      }
    }

    if (floors != null) {
      for (PlatformConfig f : floors) {
        Entity bound = ObstacleFactory.createFloor(f);
        bound.setScale((float) f.width, (float) f.height);
        entities.add(new SpawnData(f.position, bound));
      }
    }
  }

  /** Creates all the spike clusters and adds them to the entities map for the level to spawn */
  private void createSpikes() {
    if (spikes == null) {
      return;
    }

    String texture = spikeTFP != null ? spikeTFP : "images/terrain/Level_1/Level_1_Spike.png";
    for (SpikeClusterConfig s : spikes) {
      for (int i = s.xMin; i <= s.xMax; i++) {
        for (int j = s.yMin; j <= s.yMax; j++) {
          Entity spike = ObstacleFactory.createSpike(s, texture, i - s.xMin, s.xMax - s.xMin + 1);
          entities.add(new SpawnData(new GridPoint2(i, j), spike));
        }
      }
    }
  }

  private void createTraps() {
    if (ballTraps == null) {
      return;
    }

    for (SpikyBallTrapConfig b : ballTraps) {
      Entity trap = ObstacleFactory.createSpikyBallTrap(b);
      entities.add(new SpawnData(b.position, trap));
    }
  }

  private void createTriggerButtons() {
    if (triggerButtons == null) {
      return;
    }

    for (TriggerButtonConfig t : triggerButtons) {
      Entity button = ObstacleFactory.createButton(t);
      entities.add(new SpawnData(t.position, button));
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
   * Creates a new entity that triggers the next level game area to be loaded by the current game
   * screen
   */
  private void createLevelTrigger() {
    if (nextLevelTriggerSpawn == null) {
      return;
    }

    Entity trigger =
        ObstacleFactory.createNextLevelTriggerEntity(nextLevelName, nextLevelPortalHeight);
    entities.add(new SpawnData(nextLevelTriggerSpawn, trigger));
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

  /**
   * Creates all checkpoints for the level and adds them to the entities map for the level to spawn.
   */
  private void createCheckpoints() {
    if (checkpoints == null) {
      return;
    }

    for (CheckpointConfig c : checkpoints) {
      Entity checkpoint = new Entity();

      checkpoint
          .addComponent(new CheckpointComponent(false, c.getPosition(), checkpointAtlas))
          .addComponent(
              new PointLightComponent(
                  ServiceLocator.getLightingService().getEngine().getRayHandler(),
                  LightingDefaults.RAYS,
                  Color.CLEAR,
                  LightingDefaults.DIST));

      checkpoint.setScale(CheckpointComponent.ATLAS_WIDTH, CheckpointComponent.ATLAS_HEIGHT);

      // The statue art comes from the checkpoint atlas and is drawn by the checkpoint entity
      // itself. Without an atlas the checkpoint still works but has nothing to draw.
      if (checkpointAtlas != null) {
        AnimationRenderComponent animator =
            new AnimationRenderComponent(
                ServiceLocator.getResourceService().getAsset(checkpointAtlas, TextureAtlas.class));
        animator.addAnimation("unlit", 0.2f, Animation.PlayMode.LOOP);
        animator.addAnimation("lit", 0.2f, Animation.PlayMode.LOOP);
        // Behind the player, so they walk in front of the statue instead of disappearing behind it.
        animator.setFixedZIndex(CHECKPOINT_Z_INDEX);
        checkpoint.addComponent(animator);
        animator.startAnimation("unlit");
        // Keep the statue's proportions, whatever the atlas's frame shape is.
        animator.scaleEntity();
        checkpoint.scaleHeight(checkpointHeight);
      }

      GridPoint2 pos = c.getPosition();
      checkpoint.setPosition((float) pos.x, pos.y - 1.3f);

      c.setEntity(checkpoint);
      entities.add(new SpawnData(c.getPosition(), checkpoint));
    }
  }

  /** Creates all spawners in the level and adds them to the entities for the game area to spawn */
  private void createSpawners() {
    if (enemySpawners == null) {
      return;
    }

    for (EnemySpawnerConfig c : enemySpawners) {
      Entity spawner = ObstacleFactory.createEnemySpawnerEntity(c);
      entities.add(new SpawnData(c.position, spawner));
    }
  }

  /** Creates all map triggers and scales them to their correct size for the game area to spawn */
  private void createTriggers() {
    if (mapTriggers == null) {
      return;
    }

    for (TriggerConfig c : mapTriggers) {
      Entity trigger = ObstacleFactory.createTriggerEntity(c);
      trigger.setScale(c.scale);
      entities.add(new SpawnData(c.position, trigger));
    }
  }
}
