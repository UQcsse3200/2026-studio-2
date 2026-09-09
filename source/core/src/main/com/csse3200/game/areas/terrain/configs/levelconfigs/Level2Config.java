package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.configs.LevelConfig;

public class Level2Config extends LevelConfig {
  public Level2Config() {
    playerSpawn = new GridPoint2(1, 4);
    winConditionSpawn = new GridPoint2(80, 18);
  }
}
