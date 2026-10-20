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
  private static final String WATER_STOP = "waterStop";

  private static final String WATER_START = "waterStart";

  private static final String SEC3_TPS = "sec3 tps";

  private static final String ENEMY_ARENA2_COMPLETE = "enemyArena2Complete";

  private static final String ENEMY_ARENA2 = "enemyArena2";

  private static final String ENEMY_ARENA1_COMPLETE = "enemyArena1Complete";

  private static final String ENEMY_ARENA1 = "enemyArena1";

  private static final String PLATFORM_TEXTURE = "images/terrain/Level_3/Platform_level-3.png";

  private static final String BOSS_ARENA = "bossArena";

  GridPoint2 risingWaterSpawn;
  float risingWaterSpeed;

  public Level3Config(Entity player) {
    // Textures
    // TFP = Texture File Path
    platformTFP = PLATFORM_TEXTURE;
    movingPlatformTFP = PLATFORM_TEXTURE;
    crumblingPlatformTFP = "images/terrain/Level_3/Platform-crumbling-level-3.png";
    triggerablePlatformTFP = PLATFORM_TEXTURE;
    ledgesTFP = PLATFORM_TEXTURE;
    groundTFP = "images/terrain/Level_3/tile-level3.png";
    checkpointAtlas = "images/terrain/Level_1/Level_1_checkpoint.atlas";
    // Smaller than the other levels' statues, which look oversized against the narrow platforms.
    checkpointHeight = 1.4f;

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
          new PlatformConfig(new GridPoint2(24, 14), 3, 1, 0, platformTFP), // P6
          new PlatformConfig(new GridPoint2(7, 18), 3, 1, 0, platformTFP), // P7
          new PlatformConfig(new GridPoint2(20, 19), 3, 3, 8, groundTFP), // P9
          new PlatformConfig(new GridPoint2(3, 21), 3, 1, 3, platformTFP), // P10

          // arena 2
          new PlatformConfig(new GridPoint2(9, 36), 3, 1, 0, platformTFP), // P11
          new PlatformConfig(new GridPoint2(15, 36), 3, 1, 0, platformTFP), // P12
          new PlatformConfig(new GridPoint2(6, 39), 3, 1, 0, platformTFP), // P13
          new PlatformConfig(new GridPoint2(12, 40), 3, 1, 8, platformTFP), // P14
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
          new PlatformConfig(new GridPoint2(13, 87), 1, 7, 0, groundTFP), // G7
          new PlatformConfig(new GridPoint2(0, 97), 12, 1, 3, groundTFP), // G8
          new PlatformConfig(new GridPoint2(15, 97), 12, 1, 9, groundTFP), // G9
        };

    movingPlatforms =
        new MovingPlatformConfig[] {
          new MovingPlatformConfig( // MP1
              new GridPoint2(3, 12),
              3,
              1,
              2,
              movingPlatformTFP,
              new Vector2(3, 12),
              new Vector2(3, 19),
              new Vector2(0, 3),
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
              new String[] {ENEMY_ARENA1},
              false,
              true), // TP1
          new TriggerablePlatformConfig(
              new GridPoint2(3, 33),
              6,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {ENEMY_ARENA1_COMPLETE},
              false,
              true), // TP2
          new TriggerablePlatformConfig(
              new GridPoint2(12, 33),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {ENEMY_ARENA1_COMPLETE},
              false,
              true), // TP3
          new TriggerablePlatformConfig(
              new GridPoint2(18, 33),
              6,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {ENEMY_ARENA1_COMPLETE},
              false,
              true), // TP4
          new TriggerablePlatformConfig(
              new GridPoint2(12, 58),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"tp5", ENEMY_ARENA2},
              true,
              false), // TP5
          new TriggerablePlatformConfig(
              new GridPoint2(5, 64),
              5,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {ENEMY_ARENA2_COMPLETE},
              false,
              true), // TP6
          new TriggerablePlatformConfig(
              new GridPoint2(12, 64),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {ENEMY_ARENA2_COMPLETE},
              false,
              true), // TP7
          new TriggerablePlatformConfig(
              new GridPoint2(17, 64),
              5,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {ENEMY_ARENA2_COMPLETE},
              false,
              true), // TP8
          new TriggerablePlatformConfig(
              new GridPoint2(9, 87),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {SEC3_TPS},
              false,
              false), // TP9
          new TriggerablePlatformConfig(
              new GridPoint2(3, 90),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {SEC3_TPS},
              true,
              false), // TP10
          new TriggerablePlatformConfig(
              new GridPoint2(2, 93),
              3,
              1,
              1,
              triggerablePlatformTFP,
              new String[] {SEC3_TPS},
              false,
              false), // TP11
          new TriggerablePlatformConfig(
              new GridPoint2(12, 97),
              3,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {BOSS_ARENA},
              false,
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
              new GridPoint2(14, 93), 4, 1, 0, crumblingPlatformTFP, 4f, 1f, 6f), // CP5
          new CrumblingPlatformConfig(
              new GridPoint2(20, 93), 4, 1, 0, crumblingPlatformTFP, 4f, 1f, 6f), // CP6
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
          new PlatformConfig(new GridPoint2(16, 14), 3, 1, 0, platformTFP), // P6
          new PlatformConfig(new GridPoint2(12, 24), 3, 1, 0, platformTFP), // P6
          new PlatformConfig(new GridPoint2(20, 30), 3, 1, 0, ledgesTFP), // L1
          new PlatformConfig(new GridPoint2(9, 30), 3, 1, 0, ledgesTFP), // L2
          new PlatformConfig(new GridPoint2(12, 43), 3, 1, 8, platformTFP), // P14
          new PlatformConfig(new GridPoint2(5, 48), 3, 1, 0, ledgesTFP), // L3
          new PlatformConfig(new GridPoint2(2, 50), 3, 1, 0, ledgesTFP), // L3
          new PlatformConfig(new GridPoint2(12, 51), 3, 1, 0, ledgesTFP), // L4
          new PlatformConfig(
              new GridPoint2(12, 55), 3, 1, 0, ledgesTFP), // new: below enemy arena 2 (EA2)
          new PlatformConfig(new GridPoint2(0, 61), 5, 1, 0, ledgesTFP), // L5
          new PlatformConfig(new GridPoint2(22, 61), 5, 1, 0, ledgesTFP), // L6
          new PlatformConfig(new GridPoint2(11, 61), 5, 1, 0, ledgesTFP), // new: in EA2
          new PlatformConfig(new GridPoint2(15, 67), 3, 1, 0, ledgesTFP), // new: above EA2
          new PlatformConfig(new GridPoint2(12, 84), 3, 1, 0, ledgesTFP), // L5
          new PlatformConfig(new GridPoint2(9, 93), 4, 1, 0, ledgesTFP), // L6

          // The boss-arena ledges are long, so they use the repeating ground tile.
          new PlatformConfig(new GridPoint2(0, 100), 27, 1, 0, groundTFP), // BA1
          new PlatformConfig(new GridPoint2(0, 103), 27, 1, 0, groundTFP), // BA2
          new PlatformConfig(new GridPoint2(0, 106), 27, 1, 0, groundTFP), // BA3
        };

    triggerButtons =
        new TriggerButtonConfig[] {
          new TriggerButtonConfig(new GridPoint2(8, 57), 180f, false, new String[] {"tp5"}), // B1
          new TriggerButtonConfig(new GridPoint2(18, 57), 180f, false, new String[] {"tp5"}), // B2
          new TriggerButtonConfig(
              new GridPoint2(12, 89), 90f, false, new String[] {SEC3_TPS}), // B3
        };

    spikes =
        new SpikeClusterConfig[] {
          new SpikeClusterConfig(6, 6, 18, 18, 90f, false), // SC1
          new SpikeClusterConfig(9, 9, 18, 18, 270f, false), // SC2
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
              new String[] {ENEMY_ARENA2_COMPLETE},
              4f,
              true,
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
      new SpawnerConfig(
          new ArrayList<>(
              List.of(
                  () ->
                      EnemyFactory.createCalypso(
                          player,
                          List.of(
                              new Vector2(2f, 98f),
                              new Vector2(13.5f, 98f),
                              new Vector2(25f, 98f),
                              new Vector2(2f, 100f),
                              new Vector2(13.5f, 100f),
                              new Vector2(25f, 100f),
                              new Vector2(2f, 103f),
                              new Vector2(13.5f, 103f),
                              new Vector2(25f, 103f),
                              new Vector2(2f, 106f),
                              new Vector2(13.5f, 106f),
                              new Vector2(25f, 106f))))),
          0f,
          1,
          SpawnerComponent.ACTIVATION_MODE.NORMAL,
          false)
    };

    enemySpawners =
        new EnemySpawnerConfig[] {
          new EnemySpawnerConfig(
              new GridPoint2(3, 29),
              spawnerConfig[0],
              new String[] {ENEMY_ARENA1},
              new String[] {ENEMY_ARENA1_COMPLETE, WATER_START}), // ES1
          new EnemySpawnerConfig(
              new GridPoint2(24, 29),
              spawnerConfig[1],
              new String[] {ENEMY_ARENA1},
              new String[] {ENEMY_ARENA1_COMPLETE, WATER_START}), // ES2
          new EnemySpawnerConfig(
              new GridPoint2(13, 60),
              spawnerConfig[2],
              new String[] {ENEMY_ARENA2},
              new String[] {ENEMY_ARENA2_COMPLETE, WATER_START}), // ES3
          new EnemySpawnerConfig(
              new GridPoint2(3, 63),
              spawnerConfig[3],
              new String[] {ENEMY_ARENA2},
              new String[] {ENEMY_ARENA2_COMPLETE, WATER_START}), // ES4
          new EnemySpawnerConfig(
              new GridPoint2(23, 63),
              spawnerConfig[3],
              new String[] {ENEMY_ARENA2},
              new String[] {ENEMY_ARENA2_COMPLETE, WATER_START}), // ES5
          new EnemySpawnerConfig(
              new GridPoint2(18, 100),
              spawnerConfig[4],
              new String[] {BOSS_ARENA},
              new String[] {}),
        };

    mapTriggers =
        new TriggerConfig[] {
          new TriggerConfig(
              new GridPoint2(12, 30),
              new Vector2(3f, 1f),
              new String[] {ENEMY_ARENA1, WATER_STOP},
              true),
          new TriggerConfig(
              new GridPoint2(12, 61),
              new Vector2(3f, 1f),
              new String[] {ENEMY_ARENA2, WATER_STOP},
              true),
          new TriggerConfig(
              new GridPoint2(12, 99),
              new Vector2(3f, 1f),
              new String[] {BOSS_ARENA, WATER_STOP},
              true)
        };

    // Five checkpoints, one on each of the wide resting floors up the climb (never on a slippery,
    // crumbling or moving platform), so the rising water can reset from each of them.
    checkpoints =
        new CheckpointConfig[] {
          new CheckpointConfig(new GridPoint2(10, 6)),
          new CheckpointConfig(new GridPoint2(16, 37)), // on P12
          new CheckpointConfig(new GridPoint2(16, 68)), // on ledge above EA2
          new CheckpointConfig(new GridPoint2(13, 85)),
          new CheckpointConfig(new GridPoint2(13, 98)),
        };

    items =
        new HashMap<>(
            Map.of(
                new GridPoint2(18, 11), new Arrow(ItemType.STANDARD_ARROW, 25),
                new GridPoint2(4, 22), new Arrow(ItemType.FIRE_ARROW, 10),
                new GridPoint2(13, 25), new HealthPotion(5),
                new GridPoint2(13, 34), new HealthPotion(5),
                new GridPoint2(13, 45), new PoisonPotion(5),
                new GridPoint2(8, 54), new Arrow(ItemType.STANDARD_ARROW, 30),
                new GridPoint2(19, 54), new Arrow(ItemType.ICE_ARROW, 20),
                new GridPoint2(25, 85), new Arrow(ItemType.STANDARD_ARROW, 30),
                new GridPoint2(22, 94), new HealthPotion(10)));

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
