package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.*;
import com.csse3200.game.components.item.*;
import com.csse3200.game.components.item.consumables.HealthPotion;
import com.csse3200.game.components.item.consumables.PoisonPotion;
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
    platformTFP = "images/terrain/Level_3/Platform_level-3.png";
    movingPlatformTFP = "images/terrain/Level_3/Platform_level-3.png";
    crumblingPlatformTFP = "images/terrain/Level_3/Platform-crumbling-level-3.png";
    triggerablePlatformTFP = "images/terrain/Level_3/Platform_level-3.png";
    ledgesTFP = "images/terrain/Level_3/Platform_level-3.png";
    groundTFP = "images/terrain/Level_3/tile-level3.png";

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

          // arena 3
          new PlatformConfig(new GridPoint2(12, 80), 3, 1, 0, platformTFP), // P17
          new PlatformConfig(new GridPoint2(25, 84), 2, 1, 0, platformTFP), // P18
        };

    floors =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(0, 27), 12, 1, 3, groundTFP), // G1
          new PlatformConfig(new GridPoint2(15, 27), 12, 1, 9, groundTFP), // G2
          new PlatformConfig(new GridPoint2(0, 58), 12, 1, 3, groundTFP), // G3
          new PlatformConfig(new GridPoint2(15, 58), 12, 1, 9, groundTFP), // G4
          new PlatformConfig(new GridPoint2(2, 84), 10, 1, 0, groundTFP), // G5
          new PlatformConfig(new GridPoint2(15, 84), 7, 1, 0, groundTFP), // G6
          new PlatformConfig(new GridPoint2(13, 87), 1, 8, 0, groundTFP), // G7
          new PlatformConfig(new GridPoint2(0, 97), 12, 1, 3, groundTFP), // G8
          new PlatformConfig(new GridPoint2(15, 97), 12, 1, 9, groundTFP), // G9
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
          new MovingPlatformConfig( // MP4
              new GridPoint2(20, 42),
              3,
              1,
              0,
              movingPlatformTFP,
              new Vector2(20, 38),
              new Vector2(20, 45),
              new Vector2(0, 4),
              new String[] {}),
          new MovingPlatformConfig( // MP 5
              new GridPoint2(2, 75),
              3,
              1,
              0,
              movingPlatformTFP,
              new Vector2(2, 74),
              new Vector2(8, 74),
              new Vector2(2.5f, 0),
              new String[] {}),
          new MovingPlatformConfig( // MP 6
              new GridPoint2(7, 78),
              3,
              1,
              0,
              movingPlatformTFP,
              new Vector2(2, 77),
              new Vector2(8, 77),
              new Vector2(2.5f, 0),
              new String[] {}),
          new MovingPlatformConfig( // MP 7
              new GridPoint2(4, 81),
              3,
              1,
              0,
              movingPlatformTFP,
              new Vector2(2, 80),
              new Vector2(8, 80),
              new Vector2(2.5f, 0),
              new String[] {}),
          new MovingPlatformConfig( // MP 8
              new GridPoint2(16, 89),
              3,
              1,
              0,
              movingPlatformTFP,
              new Vector2(16, 89),
              new Vector2(24, 89),
              new Vector2(3f, 0),
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
              true), // TP5
          new TriggerablePlatformConfig(
              new GridPoint2(5, 64),
              5,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"enemyArena2Complete"},
              false), // TP6
          new TriggerablePlatformConfig(
              new GridPoint2(12, 64),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"enemyArena2Complete"},
              false), // TP7
          new TriggerablePlatformConfig(
              new GridPoint2(17, 64),
              5,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"enemyArena2Complete"},
              false), // TP8
          new TriggerablePlatformConfig(
              new GridPoint2(9, 87),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"sec3 tps"},
              false), // TP9
          new TriggerablePlatformConfig(
              new GridPoint2(3, 90),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"sec3 tps"},
              true), // TP10
          new TriggerablePlatformConfig(
              new GridPoint2(2, 93),
              3,
              1,
              1,
              triggerablePlatformTFP,
              new String[] {"sec3 tps"},
              false), // TP11
          new TriggerablePlatformConfig(
              new GridPoint2(12, 97),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"sec3 tps", "bossArena"},
              true), // TP12
        };

    crumblingPlatforms =
        new CrumblingPlatformConfig[] {
          new CrumblingPlatformConfig(
              new GridPoint2(18, 53), 3, 1, 1, crumblingPlatformTFP, 4f, 1.5f, 6.5f), // CP1
          new CrumblingPlatformConfig(
              new GridPoint2(17, 71), 3, 1, 0, crumblingPlatformTFP, 2.5f, 1f, 5.5f), // CP2
          new CrumblingPlatformConfig(
              new GridPoint2(21, 74), 3, 1, 0, crumblingPlatformTFP, 2.25f, 1f, 5.25f), // CP3
          new CrumblingPlatformConfig(
              new GridPoint2(19, 77), 3, 1, 0, crumblingPlatformTFP, 2f, 1f, 5f), // CP4
          new CrumblingPlatformConfig(
              new GridPoint2(17, 80), 3, 1, 0, crumblingPlatformTFP, 2f, 0.75f, 4.75f), // CP7
          new CrumblingPlatformConfig(
              new GridPoint2(15, 94), 3, 1, 0, crumblingPlatformTFP, 4f, 1f, 6f), // CP5
          new CrumblingPlatformConfig(
              new GridPoint2(22, 94), 3, 1, 0, crumblingPlatformTFP, 4f, 1f, 6f), // CP6
        };

    slipperyPlatforms =
        new SlipperyPlatformConfig[] {
          new SlipperyPlatformConfig(new GridPoint2(12, 20), 3, 1, 0, platformTFP, 5f, 0.2f), // SP1
          new SlipperyPlatformConfig(new GridPoint2(7, 45), 3, 1, 11, platformTFP, 7f, 0.1f), // P16
          new SlipperyPlatformConfig(new GridPoint2(7, 53), 3, 1, 1, platformTFP, 4f, 0.1f), // SP2
          new SlipperyPlatformConfig(
              new GridPoint2(11, 68), 3, 1, 11, platformTFP, 6f, 0.01f), // SP3
          new SlipperyPlatformConfig(new GridPoint2(7, 70), 3, 1, 0, platformTFP, 0f, 0.01f), // SP4
          new SlipperyPlatformConfig(new GridPoint2(2, 72), 3, 1, 0, platformTFP, 0f, 0.01f), // SP5
        };

    ledges =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(20, 30), 3, 1, 0, ledgesTFP), // L1
          new PlatformConfig(new GridPoint2(9, 30), 3, 1, 0, ledgesTFP), // L2
          new PlatformConfig(new GridPoint2(7, 48), 3, 1, 0, ledgesTFP), // L3
          new PlatformConfig(new GridPoint2(12, 51), 3, 1, 0, ledgesTFP), // L4
          new PlatformConfig(
              new GridPoint2(12, 55), 3, 1, 0, ledgesTFP), // new: below enemy arena 2 (EA2)
          new PlatformConfig(new GridPoint2(0, 61), 5, 1, 0, ledgesTFP), // L5
          new PlatformConfig(new GridPoint2(22, 61), 5, 1, 0, ledgesTFP), // L6
          new PlatformConfig(new GridPoint2(11, 61), 5, 1, 0, ledgesTFP), // new: in EA2
          new PlatformConfig(new GridPoint2(15, 67), 3, 1, 0, ledgesTFP), // new: above EA2
          new PlatformConfig(new GridPoint2(12, 84), 3, 1, 0, ledgesTFP), // L5
          new PlatformConfig(new GridPoint2(9, 94), 3, 1, 0, ledgesTFP), // L6
          new PlatformConfig(new GridPoint2(0, 100), 27, 1, 0, ledgesTFP), // BA1
          new PlatformConfig(new GridPoint2(0, 103), 27, 1, 0, ledgesTFP), // BA2
          new PlatformConfig(new GridPoint2(0, 106), 27, 1, 0, ledgesTFP), // BA3
        };

    triggerButtons =
        new TriggerButtonConfig[] {
          new TriggerButtonConfig(new GridPoint2(8, 57), 180f, false, new String[] {"tp5"}), // B1
          new TriggerButtonConfig(new GridPoint2(18, 57), 180f, false, new String[] {"tp5"}), // B2
          new TriggerButtonConfig(
              new GridPoint2(12, 89), 90f, false, new String[] {"sec3 tps"}), // B3
        };

    spikes =
        new SpikeClusterConfig[] {
          new SpikeClusterConfig(5, 5, 18, 18, 90f, false), // SC1
          new SpikeClusterConfig(9, 9, 18, 18, 270f, false), // SC2
          new SpikeClusterConfig(12, 14, 40, 40, 0f, true), // mp3
          new SpikeClusterConfig(11, 11, 39, 39, 90f, true), // mp3
          new SpikeClusterConfig(15, 15, 39, 39, 270f, true), // mp3
          new SpikeClusterConfig(22, 22, 84, 84, 270f, false), // G6
          new SpikeClusterConfig(24, 24, 84, 84, 90f, false), // P18
          new SpikeClusterConfig(0, 0, 68, 75, 270f, false),
          new SpikeClusterConfig(16, 18, 90, 90, 0f, true), // mp8
          new SpikeClusterConfig(15, 15, 89, 89, 90f, true), // mp8
          new SpikeClusterConfig(19, 19, 89, 89, 270f, true), // mp8
          new SpikeClusterConfig(16, 18, 88, 88, 180f, true), // mp8
        };

    ballTraps =
        new SpikyBallTrapConfig[] {
          new SpikyBallTrapConfig(
              new GridPoint2(26, 47),
              90f,
              new String[] {},
              3.5f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT1
          new SpikyBallTrapConfig(
              new GridPoint2(16, 83),
              180f,
              new String[] {"enemyArena2Complete"},
              4f,
              false,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT2
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
          false), // ES1
      new SpawnerConfig(
          new ArrayList<>(List.of(() -> EnemyFactory.createNecromancer(player))),
          12f,
          2,
          SpawnerComponent.ACTIVATION_MODE.NORMAL,
          false), // ES2
      new SpawnerConfig(
          new ArrayList<>(
              List.of(
                  () -> EnemyFactory.createVulture(player),
                  () -> EnemyFactory.createSkeletonWarrior(player),
                  () -> EnemyFactory.createSkeletonArcher(player))),
          1.5f,
          5,
          SpawnerComponent.ACTIVATION_MODE.NORMAL,
          false), // ES3
      new SpawnerConfig(
          new ArrayList<>(
              List.of(
                  () -> EnemyFactory.createVulture(player),
                  () -> EnemyFactory.createSkeletonArcher(player))),
          3f,
          4,
          SpawnerComponent.ACTIVATION_MODE.NORMAL,
          false), // ES4+5
    };

    enemySpawners =
        new EnemySpawnerConfig[] {
          new EnemySpawnerConfig(
              new GridPoint2(3, 29),
              spawnerConfig[0],
              new String[] {"enemyArena1"},
              new String[] {"enemyArena1Complete", "waterStart"}), // ES1
          new EnemySpawnerConfig(
              new GridPoint2(24, 29),
              spawnerConfig[1],
              new String[] {"enemyArena1"},
              new String[] {"enemyArena1Complete", "waterStart"}), // ES2
          new EnemySpawnerConfig(
              new GridPoint2(13, 60),
              spawnerConfig[2],
              new String[] {"enemyArena2"},
              new String[] {"enemyArena2Complete", "waterStart"}), // ES3
          new EnemySpawnerConfig(
              new GridPoint2(3, 63),
              spawnerConfig[3],
              new String[] {"enemyArena2"},
              new String[] {"enemyArena2Complete", "waterStart"}), // ES4
          new EnemySpawnerConfig(
              new GridPoint2(23, 63),
              spawnerConfig[3],
              new String[] {"enemyArena2"},
              new String[] {"enemyArena2Complete", "waterStart"}), // ES5
        };

    mapTriggers =
        new TriggerConfig[] {
          new TriggerConfig(
              new GridPoint2(12, 29),
              new Vector2(3f, 1f),
              new String[] {"enemyArena1", "waterStop"},
              true),
          new TriggerConfig(
              new GridPoint2(12, 60),
              new Vector2(3f, 1f),
              new String[] {"enemyArena2", "waterStop"},
              true),
          new TriggerConfig(
              new GridPoint2(12, 99),
              new Vector2(3f, 1f),
              new String[] {"bossArena", "waterStop"},
              true)
        };

    checkpoints =
        new CheckpointConfig[] {
          new CheckpointConfig(new GridPoint2(10, 6)),
          new CheckpointConfig(new GridPoint2(13, 21)),
          new CheckpointConfig(new GridPoint2(13, 34)),
          new CheckpointConfig(new GridPoint2(13, 52)),
          new CheckpointConfig(new GridPoint2(13, 65)),
          new CheckpointConfig(new GridPoint2(13, 85)),
          new CheckpointConfig(new GridPoint2(13, 98)),
        };

    items =
        new HashMap<>(
            Map.of(
                new GridPoint2(18, 11), new Arrow(ItemType.STANDARD_ARROW, 25),
                new GridPoint2(4, 22), new Arrow(ItemType.FIRE_ARROW, 10),
                new GridPoint2(13, 25), new HealthPotion(5),
                new GridPoint2(13, 35), new HealthPotion(5),
                new GridPoint2(13, 43), new PoisonPotion(5),
                new GridPoint2(8, 54), new Arrow(ItemType.STANDARD_ARROW, 30),
                new GridPoint2(19, 54), new Arrow(ItemType.ICE_ARROW, 20),
                new GridPoint2(25, 85), new Arrow(ItemType.STANDARD_ARROW, 30),
                new GridPoint2(23, 95), new HealthPotion(10)));

    wheelSpinSpawns = new GridPoint2[] {new GridPoint2(20, 22)};
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
