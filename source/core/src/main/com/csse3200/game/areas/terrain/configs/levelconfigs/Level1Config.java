package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.*;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.consumables.HealthPotion;
import com.csse3200.game.components.item.weapons.bow.arrow.Arrow;
import java.util.HashMap;

public class Level1Config extends LevelConfig {

  /** Creates the level 1 config */
  public Level1Config() {
    platformTFP = "images/terrain/Level_1/Level_1_platform.png";
    mossyPlatformTFP = "images/terrain/Level_1/Level_1_platform.png";
    groundTFP = "images/terrain/Level_1/Level_1_tile.png";
    spikeTFP = "images/terrain/Level_1/Level_1_Spike.png";
    checkpointAtlas = "images/terrain/Level_1/Level_1_checkpoint.atlas";

    playerSpawn = new GridPoint2(1, 4);
    nextLevelTriggerSpawn = new GridPoint2(88, 18);
    nextLevelName = "level2";

    // Retain the original switchbacks, floors and shafts; add pacing around that structure.
    platforms =
        new PlatformConfig[] {
          // first half
          new PlatformConfig(new GridPoint2(4, 2), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(8, 4), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(19, 7), 3, 1, 0, platformTFP),
          // new PlatformConfig(new GridPoint2(19, 7), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(27, 1), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(31, 2), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(34, 4), 3, 1, 2, platformTFP),
          new PlatformConfig(new GridPoint2(30, 6), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(27, 8), 3, 1, 10, platformTFP),
          new PlatformConfig(new GridPoint2(23, 10), 3, 1, 2, platformTFP),
          new PlatformConfig(new GridPoint2(12, 12), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(14, 9), 3, 1, 0, platformTFP),
          // new PlatformConfig(new GridPoint2(9, 13), 3, 1, 0, platformTFP),

          // Optional reverse climb to the concealed merchant entrance on the original roof.
          new PlatformConfig(new GridPoint2(19, 20), 3, 1, 2, platformTFP),
          // Approach step for the spike crossing; the three original upper platforms remain.
          new PlatformConfig(new GridPoint2(53, 20), 3, 1, 2, platformTFP),
          // second half
          new PlatformConfig(new GridPoint2(56, 22), 3, 1, 1, platformTFP),
          new PlatformConfig(new GridPoint2(61, 23), 3, 1, 1, platformTFP),
          new PlatformConfig(new GridPoint2(66, 22), 3, 1, 1, platformTFP),
        };

    // All moving platforms match the 3 x 1 normal platforms. The lower lift is shifted
    // one tile left so its widened body clears the original wall at x=63.
    movingPlatforms =
        new MovingPlatformConfig[] {
          moving(8, 15, 8, 15, 14, 15, 2, 0, 0),
          moving(25, 15, 25, 15, 36, 15, 3, 0, 0),
          moving(60, 10, 60, 2, 60, 10, 0, 2, 10),
          moving(60, 12, 60, 12, 60, 15, 0, 2, 0),
          moving(72, 2, 72, 2, 72, 12, 0, 2.5f, 10),
        };

    spikes =
        new SpikeClusterConfig[] {
          // Distinct upper crossing: 3 broad platforms above the original shelf.
          new SpikeClusterConfig(60, 67, 19, 19, 0f, false),
          // Short late-game jump on the lower route, with safe landings either side.
          new SpikeClusterConfig(66, 67, 1, 1, 0f, false),
        };

    bounds =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(40, 0), 50, 1, 0, groundTFP), // bottom
          new PlatformConfig(new GridPoint2(0, 27), 90, 1, 0, groundTFP), // top
          new PlatformConfig(new GridPoint2(0, 0), 1, 25, 0, groundTFP), // left
          new PlatformConfig(new GridPoint2(90, 0), 1, 25, 0, groundTFP) // right
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
          new PlatformConfig(new GridPoint2(0, 22), 17, 1, 0, groundTFP),
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

    // Main-route supplies reward traversal; elemental bundles require optional climbs.
    items = new HashMap<>();
    items.put(new GridPoint2(9, 5), new Arrow(ItemType.STANDARD_ARROW, 5));
    items.put(new GridPoint2(10, 5), new HealthPotion(1));
    items.put(new GridPoint2(28, 2), new Arrow(ItemType.ROPE_ARROW, 1));
    items.put(new GridPoint2(5, 16), new Arrow(ItemType.STANDARD_ARROW, 5));
    items.put(new GridPoint2(22, 17), new HealthPotion(1));
    items.put(new GridPoint2(30, 20), new Arrow(ItemType.STANDARD_ARROW, 5));
    items.put(new GridPoint2(46, 22), new HealthPotion(1));
    items.put(new GridPoint2(54, 17), new Arrow(ItemType.STANDARD_ARROW, 5));
    items.put(new GridPoint2(58, 11), new HealthPotion(1));
    items.put(new GridPoint2(74, 1), new Arrow(ItemType.STANDARD_ARROW, 10));
    items.put(new GridPoint2(78, 17), new HealthPotion(1));
    items.put(new GridPoint2(15, 23), new Arrow(ItemType.FIRE_ARROW, 5));
    items.put(new GridPoint2(62, 24), new Arrow(ItemType.COLD_ARROW, 5));

    // Both are earned after a substantial climb/combat section, not grouped at the start.
    checkpoints =
        new CheckpointConfig[] {
          new CheckpointConfig(new GridPoint2(47, 22)),
          new CheckpointConfig(new GridPoint2(85, 17)),
        };
  }

  private MovingPlatformConfig moving(
      int x,
      int y,
      int fromX,
      int fromY,
      int toX,
      int toY,
      float speedX,
      float speedY,
      int grapple) {
    return new MovingPlatformConfig(
        new GridPoint2(x, y),
        3,
        1,
        grapple,
        platformTFP,
        new Vector2(fromX, fromY),
        new Vector2(toX, toY),
        new Vector2(speedX, speedY),
        new String[] {});
  }
}
