package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.LevelConfig;
import com.csse3200.game.areas.terrain.configs.MovingPlatformConfig;
import com.csse3200.game.areas.terrain.configs.PlatformConfig;
import com.csse3200.game.areas.terrain.configs.SpikeClusterConfig;
import com.csse3200.game.components.item.consumables.HealthPotion;
import com.csse3200.game.components.item.weapons.*;
import java.util.HashMap;
import java.util.Map;

public class LevelTutorialConfig extends LevelConfig {
  /** Creates the tutorial level config */
  public LevelTutorialConfig() {
    platformTFP = "images/platform.png";
    groundTFP = "images/Tile_2.png";

    playerSpawn = new GridPoint2(1, 4);
    winConditionSpawn = new GridPoint2(80, 18);

    platforms =
        new PlatformConfig[] {
          // first half
          new PlatformConfig(new GridPoint2(4, 2), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(8, 3), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(14, 6), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(19, 6), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(27, 2), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(32, 3), 2, 2, 2, platformTFP),
          new PlatformConfig(new GridPoint2(30, 6), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(27, 8), 3, 1, 10, platformTFP),
          new PlatformConfig(new GridPoint2(23, 10), 3, 1, 2, platformTFP),
          new PlatformConfig(new GridPoint2(14, 11), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(9, 13), 3, 1, 0, platformTFP),

          // second half
          new PlatformConfig(new GridPoint2(56, 22), 3, 1, 1, platformTFP),
          new PlatformConfig(new GridPoint2(60, 23), 3, 1, 1, platformTFP),
          new PlatformConfig(new GridPoint2(64, 22), 3, 1, 1, platformTFP),
        };

    movingPlatforms =
        new MovingPlatformConfig[] {
          new MovingPlatformConfig(
              new GridPoint2(9, 15),
              2,
              1,
              0,
              platformTFP,
              new Vector2(9, 15),
              new Vector2(14, 15),
              new Vector2(3, 0),
              ""),
          new MovingPlatformConfig(
              new GridPoint2(25, 15),
              2,
              1,
              0,
              platformTFP,
              new Vector2(25, 15),
              new Vector2(36, 15),
              new Vector2(3, 0),
              ""),
          new MovingPlatformConfig(
              new GridPoint2(61, 10),
              1,
              2,
              10,
              platformTFP,
              new Vector2(61, 2),
              new Vector2(61, 10),
              new Vector2(0, 3),
              ""),
          new MovingPlatformConfig(
              new GridPoint2(60, 12),
              1,
              1,
              0,
              platformTFP,
              new Vector2(60, 12),
              new Vector2(60, 15),
              new Vector2(0, 3),
              ""),
          new MovingPlatformConfig(
              new GridPoint2(72, 2),
              1,
              2,
              10,
              platformTFP,
              new Vector2(72, 2),
              new Vector2(72, 12),
              new Vector2(0, 3),
              ""),
        };

    spikes =
        new SpikeClusterConfig[] {
          new SpikeClusterConfig(53, 55, 17, 17),
          new SpikeClusterConfig(59, 68, 19, 19),
          new SpikeClusterConfig(60, 62, 1, 1)
        };

    bounds =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(0, 0), 90, 1, 0, groundTFP), // bottom
          new PlatformConfig(new GridPoint2(0, 24), 90, 1, 0, groundTFP), // top
          new PlatformConfig(new GridPoint2(0, 0), 1, 22, 0, groundTFP), // left
          new PlatformConfig(new GridPoint2(90, 0), 1, 22, 0, groundTFP) // right
        };

    floors =
        new PlatformConfig[] {
          // first half
          new PlatformConfig(new GridPoint2(0, 0), 3, 3, 0, groundTFP),
          new PlatformConfig(new GridPoint2(13, 0), 12, 5, 2, groundTFP),
          new PlatformConfig(new GridPoint2(40, 0), 10, 15, 0, groundTFP),
          new PlatformConfig(new GridPoint2(40, 15), 10, 2, 8, groundTFP),
          new PlatformConfig(new GridPoint2(18, 15), 6, 2, 9, groundTFP),
          new PlatformConfig(new GridPoint2(0, 13), 8, 3, 2, groundTFP),
          new PlatformConfig(new GridPoint2(0, 16), 1, 5, 0, groundTFP),
          new PlatformConfig(new GridPoint2(0, 21), 17, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(28, 19), 4, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(35, 21), 15, 1, 0, groundTFP),

          // second half
          new PlatformConfig(new GridPoint2(53, 13), 3, 4, 0, groundTFP),
          new PlatformConfig(new GridPoint2(56, 15), 3, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(56, 16), 1, 3, 2, groundTFP),
          new PlatformConfig(new GridPoint2(53, 7), 3, 4, 0, groundTFP),
          new PlatformConfig(new GridPoint2(56, 10), 4, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(63, 0), 1, 18, 0, groundTFP),
          new PlatformConfig(new GridPoint2(59, 18), 10, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(68, 15), 3, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(71, 15), 1, 6, 0, groundTFP),
          new PlatformConfig(new GridPoint2(64, 15), 1, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(68, 11), 1, 3, 0, groundTFP),
          new PlatformConfig(new GridPoint2(68, 11), 3, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(75, 15), 3, 1, 8, groundTFP),
          new PlatformConfig(new GridPoint2(75, 12), 1, 3, 8, groundTFP),
          new PlatformConfig(new GridPoint2(78, 0), 13, 17, 0, groundTFP),
        };

    items =
        new HashMap<>(
            Map.of(
                new GridPoint2(2, 4), new RopeArr(1),
                new GridPoint2(4, 4), new StandardArr(5),
                new GridPoint2(6, 4), new FireArr(5),
                new GridPoint2(8, 4), new ColdArr(5),
                new GridPoint2(10, 4), new HealthPotion(3)));
  }
}
