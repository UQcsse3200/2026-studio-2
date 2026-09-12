package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.LevelConfig;
import com.csse3200.game.areas.terrain.configs.MovingPlatformConfig;
import com.csse3200.game.areas.terrain.configs.PlatformConfig;
import com.csse3200.game.areas.terrain.configs.TriggerButtonConfig;
import com.csse3200.game.components.item.weapons.StandardArr;
import java.util.HashMap;
import java.util.Map;

public class Level2Config extends LevelConfig {
  public Level2Config() {
    playerSpawn = new GridPoint2(1, 4);
    winConditionSpawn = new GridPoint2(80, 18);

    ledges = new PlatformConfig[] {new PlatformConfig(new GridPoint2(1, 4), 3, 1, 0)};
    floors = new PlatformConfig[] {new PlatformConfig(new GridPoint2(1, 1), 3, 1, 0)};
    // triggerablePlatforms = new TriggerablePlatformConfig[] {new TriggerablePlatformConfig(new
    // GridPoint2(1, 6), 3, 1, 0)};
    movingPlatforms =
        new MovingPlatformConfig[] {
          new MovingPlatformConfig(
              new GridPoint2(1, 6),
              3,
              1,
              0,
              new Vector2(1, 6),
              new Vector2(3, 6),
              new Vector2(3, 0),
              "moving-platform-1")
        };
    triggerButtons =
        new TriggerButtonConfig[] {
          new TriggerButtonConfig(new GridPoint2(3, 2), "moving-platform-1")
        };
    items = new HashMap<>(Map.of(new GridPoint2(2, 4), new StandardArr(99)));
  }
}
