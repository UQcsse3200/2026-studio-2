package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.*;
// import com.csse3200.game.components.item.weapons.RopeArr;
// import com.csse3200.game.components.item.weapons.StandardArr;
import com.csse3200.game.components.item.*;
import com.csse3200.game.components.item.weapons.bow.arrow.*;
import com.csse3200.game.components.level.SpawnerComponent;

public class Level2Config extends LevelConfig {

  public Level2Config() {
    // Textures
    // TFP = Texture File Path
    platformTFP = "images/Platform_level-2.png";
    movingPlatformTFP = "images/Platform_level-2.png";
    crumblingPlatformTFP = "images/Platform_level-2.png";
    triggerablePlatformTFP = "images/Platform_level-2.png";
    ledgesTFP = "images/Platform_level-2.png";
    groundTFP = "images/tile-level2.png";

    playerSpawn = new GridPoint2(0, 42);
    winConditionSpawn = new GridPoint2(33, 8);

    platforms =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(0, 40), 3, 1, 3, platformTFP), // P1
          new PlatformConfig(new GridPoint2(8, 40), 3, 1, 9, platformTFP), // P2
          new PlatformConfig(new GridPoint2(17, 39), 3, 1, 11, platformTFP), // P3
          new PlatformConfig(new GridPoint2(0, 28), 2, 1, 2, platformTFP), // P4
          new PlatformConfig(new GridPoint2(0, 7), 3, 1, 0, platformTFP), // P5
          new PlatformConfig(new GridPoint2(8, 7), 3, 1, 0, platformTFP), // P6
          new PlatformConfig(new GridPoint2(42, 7), 7, 1, 0, platformTFP), // P9
          new PlatformConfig(new GridPoint2(38, 7), 1, 1, 0, platformTFP), // P10
          new PlatformConfig(new GridPoint2(33, 7), 2, 1, 0, platformTFP), // P11
        };

    movingPlatforms =
        new MovingPlatformConfig[] {
          new MovingPlatformConfig(
              new GridPoint2(0, 11),
              3,
              1,
              1,
              movingPlatformTFP,
              new Vector2(0, 11),
              new Vector2(6, 11),
              new Vector2(3, 0),
              new String[] {}), // MP1
          new MovingPlatformConfig(
              new GridPoint2(25, 28),
              3,
              1,
              0,
              movingPlatformTFP,
              new Vector2(25, 28),
              new Vector2(33, 28),
              new Vector2(3, 0),
              new String[] {}), // MP2
          new MovingPlatformConfig(
              new GridPoint2(50, 43),
              1,
              3,
              8,
              movingPlatformTFP,
              new Vector2(50, 27),
              new Vector2(50, 43),
              new Vector2(0, 4),
              new String[] {"triggerWheelSpinPlatform"}), // MP3
          new MovingPlatformConfig(
              new GridPoint2(50, 7),
              1,
              4,
              0,
              movingPlatformTFP,
              new Vector2(50, 7),
              new Vector2(50, 29),
              new Vector2(0, 3),
              new String[] {}), // MP4
        };

    crumblingPlatforms =
        new CrumblingPlatformConfig[] {
          new CrumblingPlatformConfig(
              new GridPoint2(16, 8), 3, 1, 0, crumblingPlatformTFP, 1.25f, 0.75f, 3f), // CP1
          new CrumblingPlatformConfig(
              new GridPoint2(24, 8), 2, 1, 0, crumblingPlatformTFP, 0.75f, 0.5f, 3f), // CP2
          new CrumblingPlatformConfig(
              new GridPoint2(46, 39), 2, 1, 0, crumblingPlatformTFP, 0.5f, 0.25f, 3f), // C3
          new CrumblingPlatformConfig(
              new GridPoint2(42, 40), 2, 1, 0, crumblingPlatformTFP, 0.5f, 0.25f, 3f), // CP4
        };

    triggerablePlatforms =
        new TriggerablePlatformConfig[] {
          new TriggerablePlatformConfig(
              new GridPoint2(25, 25),
              3,
              1,
              2,
              triggerablePlatformTFP,
              new String[] {"enemyArenaComplete"},
              false), // P7
          new TriggerablePlatformConfig(
              new GridPoint2(33, 25),
              3,
              1,
              8,
              triggerablePlatformTFP,
              new String[] {"enemyArenaComplete"},
              false), // P8
          new TriggerablePlatformConfig(
              new GridPoint2(28, 40),
              4,
              1,
              0,
              triggerablePlatformTFP,
              new String[] {"triggerWheelSpinPlatform"},
              false), // TP1
          new TriggerablePlatformConfig(
              new GridPoint2(36, 43),
              3,
              1,
              1,
              triggerablePlatformTFP,
              new String[] {"triggerWheelSpinPlatform"},
              false), // TP2
        };

    floors =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(0, 35), 13, 2, 3, groundTFP), // G1
          new PlatformConfig(new GridPoint2(4, 14), 3, 15, 8, groundTFP), // G2
          new PlatformConfig(new GridPoint2(5, 20), 14, 3, 9, groundTFP), // G3
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
          new PlatformConfig(new GridPoint2(32, 7), 1, 9, 8, groundTFP), // G14
        };

    triggerButtons =
        new TriggerButtonConfig[] {
          new TriggerButtonConfig(
              new GridPoint2(25, 42), 180f, false, new String[] {"triggerWheelSpinPlatform"}), // B1
          new TriggerButtonConfig(new GridPoint2(49, 10), 90f, true, new String[] {"b2"}), // B2 (A)
          new TriggerButtonConfig(new GridPoint2(49, 9), 90f, true, new String[] {"b3"}), // B3 (B)
          new TriggerButtonConfig(new GridPoint2(49, 8), 90f, true, new String[] {"b4"}), // B4 (C)
          new TriggerButtonConfig(new GridPoint2(49, 7), 90f, true, new String[] {"b5"}), // B5 (D)
        };

    /*
    items =
        new HashMap<>(
            Map.of(
                new GridPoint2(1, 42), new RopeArr(1),
                new GridPoint2(40, 29), new StandardArr(99)));
    */

    ledges =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(29, 15), 3, 1, 0, ledgesTFP), // L1
          new PlatformConfig(new GridPoint2(42, 21), 7, 1, 0, ledgesTFP), // L2
        };

    spikes =
        new SpikeClusterConfig[] {
          new SpikeClusterConfig(0, 11, 37, 37, 0f, false), // SC1
          new SpikeClusterConfig(19, 21, 29, 29, 0f, false), // SC2
          new SpikeClusterConfig(4, 6, 29, 29, 0f, false), // SC3
          new SpikeClusterConfig(0, 2, 12, 12, 0f, true), // SC4
          new SpikeClusterConfig(50, 50, 11, 11, 0f, true), // SC5
        };

    ballTraps =
        new SpikyBallTrapConfig[] {
          new SpikyBallTrapConfig(
              new GridPoint2(7, 19),
              180f,
              new String[] {},
              1.25f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT1
          new SpikyBallTrapConfig(
              new GridPoint2(18, 19),
              180f,
              new String[] {},
              1.25f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT2
          new SpikyBallTrapConfig(
              new GridPoint2(42, 26),
              270f,
              new String[] {"b2", "b4"},
              1f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT3
          new SpikyBallTrapConfig(
              new GridPoint2(42, 24),
              270f,
              new String[] {"b3", "b4", "b5"},
              0.75f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT4
          new SpikyBallTrapConfig(
              new GridPoint2(42, 22),
              270f,
              new String[] {"b2", "b5"},
              0.5f,
              false,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT5
          new SpikyBallTrapConfig(
              new GridPoint2(42, 19),
              270f,
              new String[] {"b3"},
              0.5f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT6
          new SpikyBallTrapConfig(
              new GridPoint2(42, 17),
              270f,
              new String[] {"b2", "b5"},
              0.75f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT7
          new SpikyBallTrapConfig(
              new GridPoint2(42, 15),
              270f,
              new String[] {"b3", "b4", "b5"},
              1f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT8
          new SpikyBallTrapConfig(
              new GridPoint2(41, 13),
              180f,
              new String[] {"b2", "b4"},
              0.3f,
              false,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT9
          new SpikyBallTrapConfig(
              new GridPoint2(39, 13),
              180f,
              new String[] {"b3", "b5"},
              0.2f,
              false,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT10
          new SpikyBallTrapConfig(
              new GridPoint2(37, 13),
              180f,
              new String[] {"b2", "b5"},
              0.35f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT11
          new SpikyBallTrapConfig(
              new GridPoint2(35, 13),
              180f,
              new String[] {"b3"},
              0.25f,
              false,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT12
        };
  }
}
