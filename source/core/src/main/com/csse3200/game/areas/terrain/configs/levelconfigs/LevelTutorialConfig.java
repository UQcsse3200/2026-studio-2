package com.csse3200.game.areas.terrain.configs.levelconfigs;

import static com.csse3200.game.areas.TutorialGameArea.*;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.*;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.components.item.weapons.*;
import com.csse3200.game.entities.factories.ItemFactory;
import java.util.HashMap;

public class LevelTutorialConfig extends LevelConfig {

  /** Creates the tutorial level config */
  public LevelTutorialConfig() {
    platformTFP = "images/terrain/platform.png";
    groundTFP = "images/terrain/Tile_2.png";

    // TEMP
    playerSpawn = new GridPoint2(1, 4);
    nextLevelTriggerSpawn = new GridPoint2(80, 18);
    nextLevelName = "level2";

    platforms =
        new PlatformConfig[] {
          // first half
          new PlatformConfig(new GridPoint2(4, 2), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(8, 4), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(14, 6), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(19, 7), 3, 1, 0, platformTFP), // Use 7 not 6
          new PlatformConfig(new GridPoint2(23, 4), 3, 1, 0, platformTFP), // Bridges the big gap below
          new PlatformConfig(new GridPoint2(27, 1), 3, 1, 0, platformTFP), // Use 1 not 2
          new PlatformConfig(new GridPoint2(31, 2), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(34, 4), 3, 1, 0, platformTFP), // Use 34 not 32
          new PlatformConfig(new GridPoint2(30, 6), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(27, 8), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(23, 10), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(14, 11), 3, 1, 0, platformTFP),
          new PlatformConfig(new GridPoint2(9, 13), 3, 1, 0, platformTFP),

          // second half
          new PlatformConfig(new GridPoint2(56, 25), 3, 1, 0, platformTFP), // Use 25 not 22
          new PlatformConfig(new GridPoint2(61, 26), 3, 1, 0, platformTFP), // Use 61,26 not 60,23
          new PlatformConfig(new GridPoint2(66, 25), 3, 1, 0, platformTFP), // Use 66,25 not 64,22
        };

    movingPlatforms =
        new MovingPlatformConfig[] {
          new MovingPlatformConfig(
              new GridPoint2(9, 16), // Use 16 not 15
              2,
              1,
              0,
              platformTFP,
              new Vector2(9, 16), // Use 16 not 15
              new Vector2(14, 16), // Use 16 not 15
              new Vector2(3, 0),
              new String[] {}),
          new MovingPlatformConfig(
              new GridPoint2(25, 15),
              2,
              1,
              0,
              platformTFP,
              new Vector2(25, 15),
              new Vector2(36, 15),
              new Vector2(3, 0),
              new String[] {}),
          new MovingPlatformConfig(
              new GridPoint2(61, 10),
              1,
              2,
              10,
              platformTFP,
              new Vector2(61, 2),
              new Vector2(61, 10),
              new Vector2(0, 3),
              new String[] {}),
          new MovingPlatformConfig(
              new GridPoint2(60, 12),
              1,
              1,
              0,
              platformTFP,
              new Vector2(60, 12),
              new Vector2(60, 15),
              new Vector2(0, 3),
              new String[] {}),
          new MovingPlatformConfig(
              new GridPoint2(72, 2),
              1,
              2,
              10,
              platformTFP,
              new Vector2(72, 2),
              new Vector2(72, 12),
              new Vector2(0, 3),
              new String[] {}),

          // Spike gauntlet crossing: ferries the player over the spike strip on floor(0,22)
          new MovingPlatformConfig(
              new GridPoint2(4, 24),
              2,
              1,
              0,
              platformTFP,
              new Vector2(4, 24),
              new Vector2(12, 24),
              new Vector2(3, 0),
              new String[] {}),
        };

    spikes =
        new SpikeClusterConfig[] {
        //   new SpikeClusterConfig(53, 55, 17, 17, 0f, false),
        //   new SpikeClusterConfig(59, 68, 19, 19, 0f, false),
        //   new SpikeClusterConfig(60, 62, 1, 1, 0f, false),
        //   new SpikeClusterConfig(13, 21, 5, 5, 0f, false), // Use 21 not 23
        //   new SpikeClusterConfig(2, 13, 21, 21, 180f, false), // Use 21,21 not 20,20
        //   new SpikeClusterConfig(1, 1, 16, 20, 270f, false),
        //   new SpikeClusterConfig(39, 39, 0, 15, 90f, false),

          // first-half spike gauntlet: gap in floor(0,22) crossed via the moving platform above
          new SpikeClusterConfig(6, 10, 23, 23, 0f, false),

          //  second-half
          new SpikeClusterConfig(72, 72, 15, 20, 270f, false), // right wall of floor(71,15)
          new SpikeClusterConfig(67, 67, 11, 13, 90f, false), // left wall of notch
          new SpikeClusterConfig(68, 70, 12, 12, 0f, false), // floor of notch

          // box cluster (took me 3 hours)
          new SpikeClusterConfig(52, 52, 7, 10, 90f, false), // left wall of floor(53,7)
          new SpikeClusterConfig(53, 55, 11, 11, 0f, false), // top of floor(53,7) 3x4, facing up
          new SpikeClusterConfig(
              56, 56, 16, 18, 270f, false), // left wall of floor(56,16) 1x3 notch
          new SpikeClusterConfig(56, 58, 14, 14, 180f, false), // underside of floor(56,15) 3x1
        };

    bounds =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(40, 0), 50, 1, 0, groundTFP), // bottom
          new PlatformConfig(new GridPoint2(0, 27), 90, 1, 0, groundTFP), // top  // Use 27 not 24
          new PlatformConfig(new GridPoint2(0, 0), 1, 25, 0, groundTFP), // left  // Use 25 not 22
          new PlatformConfig(new GridPoint2(90, 0), 1, 25, 0, groundTFP) // right // Use 25 not 22
        };

    floors =
        new PlatformConfig[] {
          // first half
          new PlatformConfig(new GridPoint2(0, 0), 3, 3, 0, groundTFP),
          new PlatformConfig(new GridPoint2(13, 0), 12, 5, 0, groundTFP),
          new PlatformConfig(new GridPoint2(40, 0), 10, 15, 0, groundTFP),
          new PlatformConfig(new GridPoint2(40, 15), 10, 2, 0, groundTFP),
          new PlatformConfig(new GridPoint2(18, 15), 6, 2, 0, groundTFP),
          new PlatformConfig(new GridPoint2(0, 13), 8, 3, 0, groundTFP),
          new PlatformConfig(new GridPoint2(0, 16), 1, 5, 0, groundTFP),
          new PlatformConfig(new GridPoint2(0, 22), 17, 1, 0, groundTFP), // Use 22 not 21
          new PlatformConfig(new GridPoint2(28, 19), 4, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(35, 21), 15, 1, 0, groundTFP),

          // second half
          new PlatformConfig(new GridPoint2(53, 13), 3, 4, 0, groundTFP),
          new PlatformConfig(new GridPoint2(56, 15), 3, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(56, 16), 1, 3, 0, groundTFP),
          new PlatformConfig(new GridPoint2(53, 7), 3, 4, 0, groundTFP),
          new PlatformConfig(new GridPoint2(56, 10), 4, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(63, 0), 1, 18, 0, groundTFP),
          new PlatformConfig(new GridPoint2(59, 18), 10, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(68, 15), 3, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(71, 15), 1, 6, 0, groundTFP),
          new PlatformConfig(new GridPoint2(64, 15), 1, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(68, 11), 1, 3, 0, groundTFP),
          new PlatformConfig(new GridPoint2(68, 11), 3, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(75, 15), 3, 1, 0, groundTFP),
          new PlatformConfig(new GridPoint2(75, 12), 1, 3, 0, groundTFP),
          new PlatformConfig(new GridPoint2(78, 0), 13, 17, 0, groundTFP),
        };

    // No items are placed at the player's start; pickups are spread out further into the level.
    items = new HashMap<>();
    items.put(
        new GridPoint2(20, 8),
        ItemFactory.createHealthPotion(2).getComponent(ItemComponent.class).getItem());
    items.put(
        new GridPoint2(35, 5),
        ItemFactory.createRopeArrow(2).getComponent(ItemComponent.class).getItem());
    items.put(
        new GridPoint2(45, 17),
        ItemFactory.createHealthPotion(2).getComponent(ItemComponent.class).getItem());
    items.put(
        new GridPoint2(57, 26),
        ItemFactory.createFireArrow(3).getComponent(ItemComponent.class).getItem());
    items.put(
        new GridPoint2(67, 26),
        ItemFactory.createColdArrow(3).getComponent(ItemComponent.class).getItem());

    // Example checkpoint set to player spawn position
    checkpoints =
        new CheckpointConfig[] {
          new CheckpointConfig(new GridPoint2(1, 3)),
          //new CheckpointConfig(new GridPoint2(23, 5)),
          //new CheckpointConfig(new GridPoint2(5, 16)),
          //new CheckpointConfig(new GridPoint2(15, 12)),
          //new CheckpointConfig(new GridPoint2(47, 17)),
          new CheckpointConfig(new GridPoint2(54, 1)),
          new CheckpointConfig(new GridPoint2(73, 1)),
        };
  }
}
