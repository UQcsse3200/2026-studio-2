package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.*;
import com.csse3200.game.components.item.weapons.StandardArr;
import java.util.HashMap;
import java.util.Map;

public class Level2Config extends LevelConfig {

  public Level2Config() {

    // =========================
    // TEXTURES
    // =========================
    platformTFP = "images/Platform_level-2.png";

    // NEW LEVEL 2 GREEN GROUND TILE
    groundTFP = "images/tile-level2.png";

    // =========================
    // PLAYER START
    // =========================
    playerSpawn = new GridPoint2(2, 5);

    // =========================
    // WIN CONDITION
    // =========================
    winConditionSpawn = new GridPoint2(80, 18);

    // =========================
    // LEVEL 2 PLATFORMS
    // =========================
    platforms =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(2, 2), 3, 1, 0, platformTFP), // P1
          new PlatformConfig(new GridPoint2(10, 7), 3, 1, 0, platformTFP), // P2
          new PlatformConfig(new GridPoint2(18, 9), 3, 1, 0, platformTFP) // P3
        };

    // =========================
    // MOVING PLATFORM
    // =========================
    movingPlatforms =
        new MovingPlatformConfig[] {
          new MovingPlatformConfig(
              new GridPoint2(4, 4),
              3,
              1,
              0,
              platformTFP,
              new Vector2(4, 4),
              new Vector2(6, 4),
              new Vector2(2, 0),
              "moving-platform-1")
        };

    // =========================
    // CRUMBLING PLATFORM
    // =========================
    crumblingPlatforms =
        new CrumblingPlatformConfig[] {

          // 2 seconds before crumbling
          // 2 seconds spent crumbling
          // 3 seconds before respawning
          new CrumblingPlatformConfig(new GridPoint2(6, 2), 3, 1, 0, platformTFP, 2f, 2f, 3f)
        };

    // =========================
    // LEVEL 2 GROUND
    // =========================
    floors =
        new PlatformConfig[] {new PlatformConfig(new GridPoint2(0, -12), 39, 13, 0, groundTFP)};

    // =========================
    // TRIGGER BUTTON
    // =========================
    triggerButtons =
        new TriggerButtonConfig[] {
          new TriggerButtonConfig(new GridPoint2(6, 5), 0f, true, "moving-platform-1")
        };

    // =========================
    // ITEMS
    // =========================
    items = new HashMap<>(Map.of(new GridPoint2(2, 4), new StandardArr(99)));

    // =========================
    // LEDGES
    // =========================
    ledges = new PlatformConfig[] {new PlatformConfig(new GridPoint2(7, 3), 3, 1, 0, platformTFP)};

    // =========================
    // SPIKES
    // =========================
    spikes = new SpikeClusterConfig[] {new SpikeClusterConfig(4, 5, 5, 5, 0, true)};
  }
}
