package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.*;
import com.csse3200.game.components.item.weapons.StandardArr;
import java.util.HashMap;
import java.util.Map;

public class Level2Config extends LevelConfig {

  public Level2Config() {
    // Textures
    platformTFP = "images/Platform_level-2.png";
    groundTFP = "images/Ground_level-2.png";

    // Player starting position
    playerSpawn = new GridPoint2(2, 5);

    // Temporary win condition
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
              "")
        };

    // =========================
    // LEVEL 2 CRUMBLING PLATFORMS
    // =========================
    crumblingPlatforms =
        new CrumblingPlatformConfig[] {

          // TEST C1 - TEMPORARY: kept at the SAME height as the spawn platform (P1) with only a
          // small horizontal gap, since Level 2's gravity is strong (-50 m/s^2 in
          // PhysicsEngine.GRAVITY) and this game's big gaps (e.g. P1 -> P2) are meant to be
          // crossed with the grapple, not a plain jump. A same-height gap needs no vertical lift,
          // just walking speed carried into a short hop. Short timings (2s / 2s) so the crumbling
          // platform chain can be manually verified in-game.
          // Revert to GridPoint2(14, 8), 1.5f, 0.5f once testing is complete.
          new CrumblingPlatformConfig(new GridPoint2(6, 2), 3, 1, 0, platformTFP, 2f, 2f)
        };

    // =========================
    // TEMPORARY GROUND
    // =========================
    floors =
        new PlatformConfig[] {
          // Ground_level-2.png is 2172x724px (an exact 3:1 aspect ratio). Width/height below
          // keep that same 3:1 ratio so the image isn't stretched, and the y position is
          // shifted down so the walkable top surface still sits at y=1, same as before.
          new PlatformConfig(new GridPoint2(0, -12), 39, 13, 0, groundTFP),
        };

    triggerButtons =
        new TriggerButtonConfig[] {
          new TriggerButtonConfig(new GridPoint2(6, 5), 0f, true, "moving-platform-1")
        };

    items = new HashMap<>(Map.of(new GridPoint2(2, 4), new StandardArr(99)));

    spikes =
        new SpikeClusterConfig[] {
          // new SpikeClusterConfig(2, 4, 2, 2, 90f)
          new SpikeClusterConfig(4, 5, 5, 5, 0, true)
        };
  }
}
