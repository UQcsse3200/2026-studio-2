package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.configs.LevelConfig;
import com.csse3200.game.areas.terrain.configs.PlatformConfig;

public class BossArenaConfig extends LevelConfig {
  public BossArenaConfig() {
    platformTFP = "images/terrain/Level_3/Platform_level-3.png";
    ledgesTFP = "images/terrain/Level_3/Platform_level-3.png";
    groundTFP = "images/terrain/Level_3/tile-level3.png";

    playerSpawn = new GridPoint2(9, 2);

    floors =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(-10, 0), 30, 1, 1, groundTFP), // P9
        };

    ledges =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(-10, 4), 30, 1, 0, ledgesTFP), // L1
          new PlatformConfig(new GridPoint2(-10, 8), 30, 1, 0, ledgesTFP), // L2
          new PlatformConfig(new GridPoint2(-10, 12), 30, 1, 0, ledgesTFP), // L3
        };
  }
}
