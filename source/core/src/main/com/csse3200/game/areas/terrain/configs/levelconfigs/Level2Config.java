package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.*;
import java.util.HashMap;
import java.util.Map;

public class Level2Config extends LevelConfig {

  public Level2Config() {
    // Textures
    // TFP = Texture File Path
    platformTFP = "images/Platform_level-2.png";
    groundTFP = "images/tile-level2.png";

    playerSpawn = new GridPoint2(0, 42);
    winConditionSpawn = new GridPoint2(80, 18);

    platforms =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(0, 40), 3, 1, 3, platformTFP), // P1
          new PlatformConfig(new GridPoint2(8, 40), 3, 1, 9, platformTFP), // P2
          new PlatformConfig(new GridPoint2(17, 39), 3, 1, 11, platformTFP), // P3
          new PlatformConfig(new GridPoint2(0, 28), 2, 1, 2, platformTFP), // P4
          new PlatformConfig(new GridPoint2(0, 7), 3, 1, 0, platformTFP), // P5
          new PlatformConfig(new GridPoint2(8, 7), 3, 1, 0, platformTFP), // P6
        };

    movingPlatforms =
        new MovingPlatformConfig[] {
          new MovingPlatformConfig(
              new GridPoint2(0, 11),
              3,
              1,
              1,
              platformTFP,
              new Vector2(0, 11),
              new Vector2(6, 11),
              new Vector2(3, 0),
              ""), // MP1
          new MovingPlatformConfig(
              new GridPoint2(25, 28),
              3,
              1,
              0,
              platformTFP,
              new Vector2(25, 28),
              new Vector2(33, 28),
              new Vector2(3, 0),
              ""), // MP2
          new MovingPlatformConfig(
              new GridPoint2(50, 27),
              1,
              3,
              8,
              platformTFP,
              new Vector2(50, 17),
              new Vector2(50, 43),
              new Vector2(0, 4),
              "triggerWheelSpinPlatform") // MP3
        };

    crumblingPlatforms =
        new CrumblingPlatformConfig[] {
          new CrumblingPlatformConfig(
              new GridPoint2(16, 8), 3, 1, 0, platformTFP, 1f, 0.25f, 3f), // CP1
          new CrumblingPlatformConfig(
              new GridPoint2(24, 8), 2, 1, 0, platformTFP, 0.75f, 0.5f, 3f), // CP2
          new CrumblingPlatformConfig(
              new GridPoint2(46, 39), 2, 1, 0, platformTFP, 0.5f, 0.25f, 3f),
          new CrumblingPlatformConfig(
              new GridPoint2(42, 40), 2, 1, 0, platformTFP, 0.5f, 0.25f, 3f), // CP4
        };

    triggerablePlatforms =
        new TriggerablePlatformConfig[] {
          new TriggerablePlatformConfig(
              new GridPoint2(25, 25), 3, 1, 2, platformTFP, "enemyArenaComplete"), // P7
          new TriggerablePlatformConfig(
              new GridPoint2(33, 25), 3, 1, 8, platformTFP, "enemyArenaComplete"), // P8
          new TriggerablePlatformConfig(
              new GridPoint2(28, 40), 4, 1, 0, platformTFP, "triggerWheelSpinPlatform"), // TP1
          new TriggerablePlatformConfig(
              new GridPoint2(36, 43), 3, 1, 1, platformTFP, "triggerWheelSpinPlatform"), // TP2
        };

    floors =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(0, 35), 20, 2, 3, groundTFP), // G1
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
          new PlatformConfig(new GridPoint2(35, 14), 7, 2, 0, groundTFP), // G13
          new PlatformConfig(new GridPoint2(32, 7), 3, 9, 8, groundTFP), // G14
        };

    triggerButtons =
        new TriggerButtonConfig[] {
          new TriggerButtonConfig(new GridPoint2(25, 42), 180f, false, "triggerWheelSpinPlatform")
        };

    items = new HashMap<>(Map.of());

    ledges =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(29, 15), 3, 1, 0, platformTFP),
        };

    spikes =
        new SpikeClusterConfig[] {
          new SpikeClusterConfig(0, 11, 37, 37, 0f, false), // SC1
          new SpikeClusterConfig(19, 21, 29, 29, 0f, false), // SC2
          new SpikeClusterConfig(4, 6, 29, 29, 0f, false), // SC3
          new SpikeClusterConfig(0, 2, 12, 12, 0f, true) // SC4
        };

    ballTraps =
        new SpikyBallTrapConfig[] {
          new SpikyBallTrapConfig(new GridPoint2(7, 19), 180f, "", 1.5f), // SBT1
          new SpikyBallTrapConfig(new GridPoint2(18, 19), 180f, "", 1.5f), // SBT2
        };
  }
}
