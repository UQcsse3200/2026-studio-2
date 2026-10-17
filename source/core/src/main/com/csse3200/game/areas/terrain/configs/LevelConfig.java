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
import java.util.Map;

public class LevelConfig {
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
  protected GridPoint2 winConditionSpawn;
  protected GridPoint2[] wheelSpinSpawns;

  protected ArrayList<SpawnData> entities = new ArrayList<>();

  /**
   * Creates all entities as specified by the config file.
   *
   * @return a list of SpawnData objects containing entities and their spawn positions
   */
  public ArrayList<SpawnData> createEntities() {
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

  public GridPoint2 getPlayerSpawn() {
    return playerSpawn;
  }

  /** Returns the checkpoint components configured for this level. */
  public ArrayList<CheckpointComponent> getCheckpoints() {
    ArrayList<CheckpointComponent> checkpointComponents = new ArrayList<>();

    if (checkpoints == null) {
      return checkpointComponents;
    }

    for (CheckpointConfig c : checkpoints) {
      if (c.getEntity() != null) {
        CheckpointComponent component = c.getEntity().getComponent(CheckpointComponent.class);

        if (component != null) {
          checkpointComponents.add(component);
        }
      }
    }

    return checkpointComponents;
  }

  public GridPoint2[] getWheelSpinSpawns() {
    return wheelSpinSpawns;
  }

  private void createPlatforms() {
    if (platforms == null) {
      return;
    }

    for (PlatformConfig p : platforms) {
      Entity platform = ObstacleFactory.createPlatform(p);
      platform.setScale(p.width, p.height);
      entities.add(new SpawnData(p.position, platform));
    }
  }

  private void createMovingPlatforms() {
    if (movingPlatforms == null) {
      return;
    }

    for (MovingPlatformConfig p : movingPlatforms) {
      Entity platform = ObstacleFactory.createMovingPlatform(p);
      platform.setScale(p.width, p.height);
      entities.add(new SpawnData(p.position, platform));
    }
  }

  private void createCrumblingPlatforms() {
    if (crumblingPlatforms == null) {
      return;
    }

    for (CrumblingPlatformConfig c : crumblingPlatforms) {
      Entity platform = ObstacleFactory.createCrumblingPlatform(c);
      platform.setScale(c.width, c.height);
      entities.add(new SpawnData(c.position, platform));
    }
  }

  private void createTriggerablePlatforms() {
    if (triggerablePlatforms == null) {
      return;
    }

    for (TriggerablePlatformConfig t : triggerablePlatforms) {
      Entity triggerablePlatform = ObstacleFactory.createTriggerablePlatform(t);
      triggerablePlatform.setScale(t.width, t.height);
      entities.add(new SpawnData(t.position, triggerablePlatform));
    }
  }

  private void createSlipperyPlatforms() {
    if (slipperyPlatforms == null) {
      return;
    }

    for (SlipperyPlatformConfig s : slipperyPlatforms) {
      Entity slipperyPlatform = ObstacleFactory.createSlipperyPlatform(s);
      slipperyPlatform.setScale(s.width, s.height);
      entities.add(new SpawnData(s.position, slipperyPlatform));
    }
  }

  private void createLedges() {
    if (ledges == null) {
      return;
    }

    for (PlatformConfig p : ledges) {
      Entity ledge = ObstacleFactory.createLedge(p);
      ledge.setScale(p.width, p.height);
      entities.add(new SpawnData(p.position, ledge));
    }
  }

  private void createFloors() {
    if (bounds != null) {
      for (PlatformConfig b : bounds) {
        Entity bound = ObstacleFactory.createFloor(b);
        bound.setScale(b.width, b.height);
        entities.add(new SpawnData(b.position, bound));
      }
    }

    if (floors != null) {
      for (PlatformConfig f : floors) {
        Entity bound = ObstacleFactory.createFloor(f);
        bound.setScale(f.width, f.height);
        entities.add(new SpawnData(f.position, bound));
      }
    }
  }

  private void createSpikes() {
    if (spikes == null) {
      return;
    }

    for (SpikeClusterConfig s : spikes) {
      for (int i = s.xMin; i <= s.xMax; i++) {
        for (int j = s.yMin; j <= s.yMax; j++) {
          Entity spike =
              ObstacleFactory.createSpike(
                  s, spikeTFP != null ? spikeTFP : "images/terrain/Level_1/Level_1_Spike.png");

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

  private void createWinCondition() {
    if (winConditionSpawn == null) {
      return;
    }

    Entity winCon = ObstacleFactory.createWinConEntity();
    entities.add(new SpawnData(winConditionSpawn, winCon));
  }

  private void createLevelTrigger() {
    if (nextLevelTriggerSpawn == null) {
      return;
    }

    Entity trigger = ObstacleFactory.createNextLevelTriggerEntity(nextLevelName);
    entities.add(new SpawnData(nextLevelTriggerSpawn, trigger));
  }

  private void createItems() {
    if (items == null) {
      return;
    }

    for (Map.Entry<GridPoint2, Item> i : items.entrySet()) {
      Entity item = ItemFactory.createItem(i.getValue());
      entities.add(new SpawnData(i.getKey(), item));
    }
  }

  /** Creates checkpoints using a single entity for both logic and rendering. */
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
      ;

      checkpoint.setScale(CheckpointComponent.ATLAS_WIDTH, CheckpointComponent.ATLAS_HEIGHT);

      if (checkpointAtlas != null) {
        TextureAtlas atlas =
            ServiceLocator.getResourceService().getAsset(checkpointAtlas, TextureAtlas.class);

        AnimationRenderComponent animator = new AnimationRenderComponent(atlas);

        animator.addAnimation("unlit", 0.2f, Animation.PlayMode.LOOP);
        animator.addAnimation("lit", 0.2f, Animation.PlayMode.LOOP);

        checkpoint.addComponent(animator);
        animator.startAnimation("unlit");
      }

      GridPoint2 pos = c.getPosition();
      checkpoint.setPosition(pos.x, pos.y - 1.3f);

      c.setEntity(checkpoint);
      entities.add(new SpawnData(c.getPosition(), checkpoint));
    }
  }

  private void createSpawners() {
    if (enemySpawners == null) {
      return;
    }

    for (EnemySpawnerConfig c : enemySpawners) {
      Entity spawner = ObstacleFactory.createEnemySpawnerEntity(c);
      entities.add(new SpawnData(c.position, spawner));
    }
  }

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
