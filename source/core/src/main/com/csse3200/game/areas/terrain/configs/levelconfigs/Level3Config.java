package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.*;
// import com.csse3200.game.components.item.weapons.RopeArr;
// import com.csse3200.game.components.item.weapons.StandardArr;
import com.csse3200.game.components.item.*;
import com.csse3200.game.components.item.weapons.bow.arrow.*;
import com.csse3200.game.components.level.SpawnerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Level3Config extends LevelConfig {
  GridPoint2 risingWaterSpawn;
  float risingWaterSpeed;

  public Level3Config(Entity player) {
    // Textures
    // TFP = Texture File Path
    platformTFP = "images/Platform_level-3.png";
    movingPlatformTFP = "images/Platform_level-3.png";
    crumblingPlatformTFP = "images/Platform-crumbling-level-3.png";
    triggerablePlatformTFP = "images/Platform_level-3.png";
    ledgesTFP = "images/Platform_level-3.png";
    groundTFP = "images/tile-level3.png";

    playerSpawn = new GridPoint2(9, 6);
    winConditionSpawn = new GridPoint2(33, 8);
    risingWaterSpawn = new GridPoint2(0, -10);
    risingWaterSpeed = 0.3f;

    platforms =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(9, 5), 3, 1, 0, platformTFP), // P1
          new PlatformConfig(new GridPoint2(13, 7), 3, 1, 0, platformTFP), // P2
          new PlatformConfig(new GridPoint2(6, 10), 3, 1, 11, platformTFP), // P3
          new PlatformConfig(new GridPoint2(17, 10), 3, 1, 0, platformTFP), // P4
          new PlatformConfig(new GridPoint2(12, 12), 3, 1, 0, platformTFP), // P5
          new PlatformConfig(new GridPoint2(16, 15), 3, 1, 0, platformTFP), // P6
          new PlatformConfig(new GridPoint2(6, 18), 3, 1, 0, platformTFP), // P7
          new PlatformConfig(new GridPoint2(20, 19), 1, 3, 8, groundTFP), // P9
          new PlatformConfig(new GridPoint2(3, 21), 3, 1, 3, platformTFP), // P10

          // arena 2
          new PlatformConfig(new GridPoint2(9, 36), 3, 1, 0, platformTFP), // P11
          new PlatformConfig(new GridPoint2(15, 36), 3, 1, 0, platformTFP), // P12
          new PlatformConfig(new GridPoint2(6, 39), 3, 1, 0, platformTFP), // P13
          new PlatformConfig(new GridPoint2(12, 42), 3, 1, 8, platformTFP), // P14
          new PlatformConfig(new GridPoint2(16, 45), 3, 1, 0, platformTFP), // P15
        };

    floors =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(0, 27), 12, 1, 3, groundTFP), // G1
          new PlatformConfig(new GridPoint2(15, 27), 12, 1, 9, groundTFP), // G2
          new PlatformConfig(new GridPoint2(0, 58), 12, 1, 3, groundTFP), // G3
          new PlatformConfig(new GridPoint2(15, 58), 12, 1, 9, groundTFP),
        };

    movingPlatforms =
        new MovingPlatformConfig[] {
          new MovingPlatformConfig( // MP1
              new GridPoint2(3, 12),
              1,
              3,
              2,
              movingPlatformTFP,
              new Vector2(3, 12),
              new Vector2(3, 19),
              new Vector2(0, 3),
              new String[] {}),
          new MovingPlatformConfig( // MP2
              new GridPoint2(12, 24),
              3,
              1,
              1,
              movingPlatformTFP,
              new Vector2(12, 24),
              new Vector2(19, 24),
              new Vector2(3, 0),
              new String[] {}),
          new MovingPlatformConfig( // MP3
              new GridPoint2(12, 39),
              3,
              1,
              0,
              movingPlatformTFP,
              new Vector2(10, 39),
              new Vector2(17, 39),
              new Vector2(3, 0),
              new String[] {}),
          new MovingPlatformConfig(
              new GridPoint2(20, 42),
              3,
              1,
              0,
              movingPlatformTFP,
              new Vector2(20, 38),
              new Vector2(20, 45),
              new Vector2(0, 4),
              new String[] {})
        };

    triggerablePlatforms =
        new TriggerablePlatformConfig[] {
          new TriggerablePlatformConfig(
              new GridPoint2(12, 27),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"enemyArena1"},
              false), // TP1
          new TriggerablePlatformConfig(
              new GridPoint2(3, 33),
              6,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"enemyArena1Complete"},
              false), // TP2
          new TriggerablePlatformConfig(
              new GridPoint2(12, 33),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"enemyArena1Complete"},
              false), // TP3
          new TriggerablePlatformConfig(
              new GridPoint2(18, 33),
              6,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"enemyArena1Complete"},
              false), // TP4
          new TriggerablePlatformConfig(
              new GridPoint2(12, 58),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"tp5", "enemyArena2"},
              true) // TP5
        };

    ledges =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(20, 30), 3, 1, 0, ledgesTFP), // L1
          new PlatformConfig(new GridPoint2(9, 30), 3, 1, 0, ledgesTFP), // L2
          new PlatformConfig(new GridPoint2(7, 48), 3, 1, 0, ledgesTFP), // L3
          new PlatformConfig(new GridPoint2(12, 51), 3, 1, 0, ledgesTFP), // L4
          new PlatformConfig(new GridPoint2(12, 55), 3, 1, 0, ledgesTFP), // new ledge not in design
        };

    triggerButtons =
        new TriggerButtonConfig[] {
          new TriggerButtonConfig(new GridPoint2(8, 57), 180f, false, new String[] {"tp5"}), // B1
          new TriggerButtonConfig(new GridPoint2(18, 57), 180f, false, new String[] {"tp5"}), // B2
        };

    items = new HashMap<>(Map.of(new GridPoint2(9, 7), new Arrow(ItemType.ROPE_ARROW, 1)));

    spikes =
        new SpikeClusterConfig[] {
          new SpikeClusterConfig(5, 5, 18, 18, 90f, false), // SC1
          new SpikeClusterConfig(9, 9, 18, 18, 270f, false), // SC2
          new SpikeClusterConfig(12, 14, 40, 40, 0f, true), // mp3
          new SpikeClusterConfig(11, 11, 39, 39, 90f, true), // mp3
          new SpikeClusterConfig(15, 15, 39, 39, 270f, true), // mp3
        };

    ballTraps =
        new SpikyBallTrapConfig[] {
          new SpikyBallTrapConfig(
              new GridPoint2(27, 47),
              90f,
              new String[] {},
              3.5f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT1
        };

    checkpoints =
        new CheckpointConfig[] {
          new CheckpointConfig(new GridPoint2(10, 6)), new CheckpointConfig(new GridPoint2(13, 21)),
        };

    crumblingPlatforms =
        new CrumblingPlatformConfig[] {
          new CrumblingPlatformConfig(
              new GridPoint2(18, 53), 3, 1, 1, crumblingPlatformTFP, 4f, 1.5f, 6.5f), // CP1
        };

    slipperyPlatforms =
        new SlipperyPlatformConfig[] {
          new SlipperyPlatformConfig(new GridPoint2(12, 20), 3, 1, 0, platformTFP, 3f, 0.2f), // SP1
          new SlipperyPlatformConfig(new GridPoint2(7, 45), 3, 1, 11, platformTFP, 5f, 0.1f), // P16
          new SlipperyPlatformConfig(new GridPoint2(7, 53), 3, 1, 1, platformTFP, 2f, 0.1f) // SP2
        };

    // contains spawner data for the enemy spawners
    SpawnerConfig[] spawnerConfig = {
      new SpawnerConfig(
          new ArrayList<>(
              List.of(
                  () -> EnemyFactory.createVulture(player),
                  () -> EnemyFactory.createSkeletonWarrior(player))),
          4f,
          4,
          SpawnerComponent.ACTIVATION_MODE.NORMAL,
          false),
      new SpawnerConfig(
          new ArrayList<>(List.of(() -> EnemyFactory.createNecromancer(player))),
          12f,
          2,
          SpawnerComponent.ACTIVATION_MODE.NORMAL,
          false),
    };

    enemySpawners =
        new EnemySpawnerConfig[] {
          new EnemySpawnerConfig(
              new GridPoint2(3, 29),
              spawnerConfig[0],
              new String[] {"enemyArena1"},
              new String[] {"enemyArena1Complete"}),
          new EnemySpawnerConfig(
              new GridPoint2(24, 29),
              spawnerConfig[1],
              new String[] {"enemyArena1"},
              new String[] {"enemyArena1Complete"})
        };

    mapTriggers =
        new TriggerConfig[] {
          new TriggerConfig(
              new GridPoint2(12, 29), new Vector2(3f, 1f), new String[] {"enemyArena1"}, true),
          new TriggerConfig(
              new GridPoint2(12, 60), new Vector2(3f, 1f), new String[] {"enemyArena2"}, true),
        };
  }

  /**
   * Public getter for the rising water entity spawn for a level config
   *
   * @return The spawn coordinates for the rising water entity stored by this config as a GridPoint2
   */
  public GridPoint2 getRisingWaterSpawn() {
    return risingWaterSpawn;
  }

  /**
   * Public getter for the rising water entity's base speed specified by the level config
   *
   * @return The base speed of the water as a float that represents how many units should be moved
   *     up in each second
   */
  public float getWaterSpeed() {
    return risingWaterSpeed;
  }
}
