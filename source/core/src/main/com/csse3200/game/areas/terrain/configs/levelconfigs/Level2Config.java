package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.*;
import com.csse3200.game.areas.terrain.configs.PlatformConfig;
import com.csse3200.game.components.item.*;
import com.csse3200.game.components.item.consumables.HealthPotion;
import com.csse3200.game.components.item.weapons.bow.arrow.*;
import com.csse3200.game.components.level.SpawnerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Level2Config extends LevelConfig {
  private static final String TRIGGER_WHEEL_SPIN_PLATFORM = "triggerWheelSpinPlatform";

  private static final String PLATFORM_TEXTURE = "images/terrain/Level_2/level_2_platform.png";

  private static final String ENEMY_ARENA = "enemyArena";

  private static final String ENEMY_ARENA_COMPLETE = "enemyArenaComplete";

  private static final String END_PUZZLE = "endPuzzle";

  public Level2Config(Entity player) {
    // Textures
    // TFP = Texture File Path
    platformTFP = PLATFORM_TEXTURE;
    movingPlatformTFP = PLATFORM_TEXTURE;
    crumblingPlatformTFP = PLATFORM_TEXTURE;
    triggerablePlatformTFP = PLATFORM_TEXTURE;
    ledgesTFP = PLATFORM_TEXTURE;
    groundTFP = "images/terrain/Level_2/level_2_tile.png";
    spikeTFP = "images/terrain/Level_2/level_2_spikes.png";
    checkpointAtlas = "images/terrain/Level_2/level_2_checkpoint.atlas";

    playerSpawn = new GridPoint2(0, 42);
    // No nextLevelTriggerSpawn: the exit door spawned by Level2GameArea is the only Level 3
    // trigger. A config trigger at (33, 8) reached down to P11's underside and fired from below.
    nextLevelName = "level3";
    // The platform under the portal is only 2 wide, so the portal is sized to fit it.
    nextLevelPortalHeight = 1.9f;

    platforms =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(9, 26), 3, 1, 3, platformTFP),
          new PlatformConfig(new GridPoint2(14, 26), 3, 1, 3, platformTFP),
          new PlatformConfig(new GridPoint2(17, 31), 3, 1, 3, platformTFP),
          new PlatformConfig(new GridPoint2(17, 37), 3, 1, 11, platformTFP), // P3
          new PlatformConfig(new GridPoint2(0, 26), 2, 1, 2, platformTFP), // P4
          new PlatformConfig(new GridPoint2(0, 7), 3, 1, 0, platformTFP), // P5
          new PlatformConfig(new GridPoint2(5, 8), 3, 1, 0, platformTFP), // P6
          new PlatformConfig(new GridPoint2(11, 7), 3, 1, 0, platformTFP), // P6
          new PlatformConfig(new GridPoint2(20, 5), 3, 1, 0, ledgesTFP), // P6
          new PlatformConfig(new GridPoint2(29, 5), 3, 1, 0, ledgesTFP), // P6
          new PlatformConfig(new GridPoint2(42, 7), 7, 1, 0, platformTFP), // P9
          new PlatformConfig(new GridPoint2(38, 7), 1, 1, 0, platformTFP), // P10
          new PlatformConfig(new GridPoint2(33, 7), 2, 1, 0, platformTFP), // P11
        };

    movingPlatforms =
        new MovingPlatformConfig[] {
          new MovingPlatformConfig(
              new PlatformConfig(new GridPoint2(25, 28), 3, 1, 0, movingPlatformTFP),
              new Vector2(25, 28),
              new Vector2(33, 28),
              new Vector2(3, 0),
              new String[] {}), // MP2
          new MovingPlatformConfig(
              new PlatformConfig(new GridPoint2(50, 43), 3, 1, 8, movingPlatformTFP),
              new Vector2(50, 27),
              new Vector2(50, 43),
              new Vector2(0, 4),
              new String[] {TRIGGER_WHEEL_SPIN_PLATFORM}), // MP3
          new MovingPlatformConfig(
              new PlatformConfig(new GridPoint2(50, 7), 1, 4, 0, movingPlatformTFP),
              new Vector2(50, 7),
              new Vector2(50, 29),
              new Vector2(0, 3),
              new String[] {}), // MP4
        };

    crumblingPlatforms =
        new CrumblingPlatformConfig[] {
          new CrumblingPlatformConfig(
              new PlatformConfig(new GridPoint2(16, 8), 3, 1, 0, crumblingPlatformTFP),
              2.5f,
              1f,
              4f), // CP1
          new CrumblingPlatformConfig(
              new PlatformConfig(new GridPoint2(24, 8), 3, 1, 0, crumblingPlatformTFP),
              2.25f,
              0.75f,
              3.5f), // CP2
          new CrumblingPlatformConfig(
              new PlatformConfig(new GridPoint2(46, 39), 2, 1, 0, crumblingPlatformTFP),
              1.5f,
              1f,
              3f), // C3
          new CrumblingPlatformConfig(
              new PlatformConfig(new GridPoint2(42, 40), 2, 1, 0, crumblingPlatformTFP),
              1.5f,
              1f,
              3f), // CP4
        };

    triggerablePlatforms =
        new TriggerablePlatformConfig[] {
          new TriggerablePlatformConfig(
              new PlatformConfig(new GridPoint2(25, 25), 3, 1, 2, triggerablePlatformTFP),
              new String[] {ENEMY_ARENA_COMPLETE},
              false,
              true), // P7
          new TriggerablePlatformConfig(
              new PlatformConfig(new GridPoint2(33, 25), 3, 1, 8, triggerablePlatformTFP),
              new String[] {ENEMY_ARENA_COMPLETE},
              false,
              true), // P8
          new TriggerablePlatformConfig(
              new PlatformConfig(new GridPoint2(28, 40), 4, 1, 0, triggerablePlatformTFP),
              new String[] {TRIGGER_WHEEL_SPIN_PLATFORM},
              false,
              true), // TP1
          new TriggerablePlatformConfig(
              new PlatformConfig(new GridPoint2(36, 43), 3, 1, 1, triggerablePlatformTFP),
              new String[] {TRIGGER_WHEEL_SPIN_PLATFORM},
              false,
              true), // TP2
        };

    bounds = new PlatformConfig[] {new PlatformConfig(new GridPoint2(-1, -5), 1, 55, 0, groundTFP)};

    floors =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(0, 33), 13, 7, 3, groundTFP), // G1
          new PlatformConfig(new GridPoint2(4, 14), 2, 12, 8, groundTFP), // G2
          new PlatformConfig(new GridPoint2(5, 20), 14, 4, 9, groundTFP), // G3
          new PlatformConfig(new GridPoint2(19, 14), 3, 15, 0, groundTFP), // G4
          new PlatformConfig(new GridPoint2(22, 14), 7, 2, 0, groundTFP), // G5
          new PlatformConfig(new GridPoint2(22, 25), 3, 10, 0, groundTFP), // G6
          new PlatformConfig(new GridPoint2(22, 35), 3, 4, 8, groundTFP), // G7
          new PlatformConfig(new GridPoint2(22, 39), 3, 6, 0, groundTFP), // G8
          new PlatformConfig(new GridPoint2(25, 43), 1, 2, 0, groundTFP), // G9
          new PlatformConfig(new GridPoint2(26, 37), 2, 8, 0, groundTFP), // G10
          new PlatformConfig(new GridPoint2(36, 25), 3, 3, 0, groundTFP), // G11
          new PlatformConfig(new GridPoint2(39, 15), 3, 13, 0, groundTFP), // G12
          new PlatformConfig(new GridPoint2(33, 14), 9, 2, 0, groundTFP), // G13
          new PlatformConfig(new GridPoint2(32, 4), 1, 13, 8, groundTFP), // G14
        };

    triggerButtons =
        new TriggerButtonConfig[] {
          new TriggerButtonConfig(
              new GridPoint2(25, 42),
              180f,
              false,
              new String[] {TRIGGER_WHEEL_SPIN_PLATFORM}), // B1
          new TriggerButtonConfig(
              new GridPoint2(49, 9), 90f, true, new String[] {END_PUZZLE}), // B3 (B)
        };

    items =
        new HashMap<>(
            Map.of(
                new GridPoint2(40, 29), new Arrow(ItemType.STANDARD_ARROW, 99),
                new GridPoint2(10, 27), new HealthPotion(5),
                new GridPoint2(18, 32), new HealthPotion(5),
                new GridPoint2(45, 8), new HealthPotion(5),
                new GridPoint2(34, 20), new HealthPotion(5)));

    ledges =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(11, 7), 3, 1, 0, ledgesTFP), // P6
          new PlatformConfig(new GridPoint2(29, 9), 3, 1, 0, ledgesTFP), // P6
          new PlatformConfig(new GridPoint2(29, 12), 3, 1, 0, ledgesTFP), // P6
          new PlatformConfig(new GridPoint2(29, 15), 3, 1, 0, ledgesTFP), // L1
          new PlatformConfig(new GridPoint2(42, 21), 7, 1, 0, ledgesTFP), // L2
          new PlatformConfig(new GridPoint2(23, 19), 4, 1, 0, ledgesTFP),
          new PlatformConfig(new GridPoint2(33, 19), 4, 1, 0, ledgesTFP),
          new PlatformConfig(new GridPoint2(29, 22), 3, 1, 0, ledgesTFP),
          new PlatformConfig(new GridPoint2(25, 31), 3, 1, 0, ledgesTFP),
        };

    spikes =
        new SpikeClusterConfig[] {
          new SpikeClusterConfig(0, 11, 37, 37, 0f, false), // SC1
          new SpikeClusterConfig(19, 21, 29, 29, 0f, false), // SC2
          new SpikeClusterConfig(4, 5, 26, 26, 0f, false), // SC3
          new SpikeClusterConfig(0, 1, 21, 21, 0f, false), // SC3
          new SpikeClusterConfig(3, 4, 15, 15, 0f, false), // SC3

          // new SpikeClusterConfig(0, 2, 12, 12, 0f, true), // SC4
          new SpikeClusterConfig(50, 50, 11, 11, 0f, true), // SC5
        };

    ballTraps =
        new SpikyBallTrapConfig[] {
          new SpikyBallTrapConfig(
              new GridPoint2(9, 19),
              180f,
              new String[] {},
              1.25f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT1
          new SpikyBallTrapConfig(
              new GridPoint2(14, 19),
              180f,
              new String[] {},
              1.25f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT2
          new SpikyBallTrapConfig(
              new GridPoint2(42, 26),
              270f,
              new String[] {END_PUZZLE},
              1f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT3
          new SpikyBallTrapConfig(
              new GridPoint2(42, 24),
              270f,
              new String[] {END_PUZZLE},
              0.75f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT4
          new SpikyBallTrapConfig(
              new GridPoint2(42, 22),
              270f,
              new String[] {END_PUZZLE},
              0.5f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT5
          new SpikyBallTrapConfig(
              new GridPoint2(42, 19),
              270f,
              new String[] {END_PUZZLE},
              0.5f,
              false,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT6
          new SpikyBallTrapConfig(
              new GridPoint2(42, 17),
              270f,
              new String[] {END_PUZZLE},
              0.75f,
              false,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT7
          new SpikyBallTrapConfig(
              new GridPoint2(42, 15),
              270f,
              new String[] {END_PUZZLE},
              1f,
              false,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT8
          new SpikyBallTrapConfig(
              new GridPoint2(41, 13),
              180f,
              new String[] {END_PUZZLE},
              0.3f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT9
          new SpikyBallTrapConfig(
              new GridPoint2(39, 13),
              180f,
              new String[] {END_PUZZLE},
              0.2f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT10
          new SpikyBallTrapConfig(
              new GridPoint2(37, 13),
              180f,
              new String[] {END_PUZZLE},
              0.35f,
              false,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT11
          new SpikyBallTrapConfig(
              new GridPoint2(35, 13),
              180f,
              new String[] {END_PUZZLE},
              0.25f,
              false,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT12
        };

    // contains spawner data for the enemy spawners
    SpawnerConfig[] spawnerConfig = {
      new SpawnerConfig(
          new ArrayList<>(
              List.of(
                  () -> EnemyFactory.createVulture(player), // double up to make it more likely to
                  () -> EnemyFactory.createVulture(player), // spawn vultures and warriors
                  () -> EnemyFactory.createSkeletonWarrior(player),
                  () -> EnemyFactory.createSkeletonWarrior(player),
                  () -> EnemyFactory.createSkeletonArcher(player),
                  () -> EnemyFactory.createNecromancer(player))),
          7f,
          5,
          SpawnerComponent.ACTIVATION_MODE.NORMAL,
          false), // ES1+2
    };

    enemySpawners =
        new EnemySpawnerConfig[] {
          new EnemySpawnerConfig(
              new GridPoint2(24, 17),
              spawnerConfig[0],
              new String[] {ENEMY_ARENA},
              new String[] {ENEMY_ARENA_COMPLETE}), // ES1
          new EnemySpawnerConfig(
              new GridPoint2(34, 17),
              spawnerConfig[0],
              new String[] {ENEMY_ARENA},
              new String[] {ENEMY_ARENA_COMPLETE}), // ES2
        };

    mapTriggers =
        new TriggerConfig[] {
          new TriggerConfig(
              new GridPoint2(30, 16), new Vector2(3f, 1f), new String[] {ENEMY_ARENA}, true),
        };

    // Five checkpoints on stable platforms (never moving, crumbling or trap-adjacent ones),
    // spread over the level: past the start platforms, the bridge between the two pillars, the
    // bottom-left ledge before the crumbling crossing, the right-hand tower, and the bottom-right
    // ledge.
    checkpoints =
        new CheckpointConfig[] {
          new CheckpointConfig(new GridPoint2(8, 40)),
          new CheckpointConfig(new GridPoint2(11, 24)),
          new CheckpointConfig(new GridPoint2(6, 9)),
          new CheckpointConfig(new GridPoint2(38, 28)),
          new CheckpointConfig(new GridPoint2(44, 8)),
        };

    wheelSpinSpawns = new GridPoint2[] {new GridPoint2(29, 41)};
  }
}
