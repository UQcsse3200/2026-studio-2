package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.configs.LevelConfig;
import com.csse3200.game.areas.terrain.configs.PlatformConfig;

public class Level2Config extends LevelConfig {
  public Level2Config() {
    playerSpawn = new GridPoint2(1, 4);
    winConditionSpawn = new GridPoint2(80, 18);

    ledges = new PlatformConfig[] {new PlatformConfig(new GridPoint2(1, 4), 3, 1, 0)};

    floors = new PlatformConfig[] {new PlatformConfig(new GridPoint2(1, 1), 3, 1, 0)};
  }
}
