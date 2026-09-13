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
    // TFP = Texture File Path
    platformTFP = "images/Platform_level-2.png";
    groundTFP = "images/tile-level2.png";

    playerSpawn = new GridPoint2(2, 5);
    winConditionSpawn = new GridPoint2(80, 18);

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
              "moving-platform-1")
        };

    crumblingPlatforms =
        new CrumblingPlatformConfig[] {
          new CrumblingPlatformConfig(new GridPoint2(6, 2), 3, 1, 0, platformTFP, 0.5f, 0.5f, 3f)
        };

    floors =
        new PlatformConfig[] {new PlatformConfig(new GridPoint2(0, -12), 39, 13, 0, groundTFP)};

    triggerButtons =
        new TriggerButtonConfig[] {
          new TriggerButtonConfig(new GridPoint2(6, 5), 0f, true, "moving-platform-1")
        };

    items = new HashMap<>(Map.of(new GridPoint2(2, 4), new StandardArr(99)));

    ledges = new PlatformConfig[] {new PlatformConfig(new GridPoint2(7, 3), 3, 1, 0, platformTFP)};

    spikes = new SpikeClusterConfig[] {new SpikeClusterConfig(4, 5, 5, 5, 0, true)};
  }
}
