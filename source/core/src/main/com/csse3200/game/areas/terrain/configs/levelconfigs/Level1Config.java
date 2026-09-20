package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.configs.*;
import com.csse3200.game.components.item.ItemComponent;
import com.csse3200.game.entities.factories.ItemFactory;
import java.util.HashMap;

public class Level1Config extends LevelConfig {

  /** Creates the level 1 config */
  public Level1Config() {
    platformTFP = "images/terrain/cave_platform.png";
    mossyPlatformTFP = "images/terrain/cave_mossy.png";
    groundTFP = "images/terrain/cave_tile.png";

    // TEMP
    playerSpawn = new GridPoint2(1, 3);
    nextLevelTriggerSpawn = new GridPoint2(80, 18);
    nextLevelName = "level2";

    // Layout cleared for the level redesign — platforms, floors, moving platforms and spikes
    // are being rebuilt from scratch.
    platforms = new PlatformConfig[] {};

    movingPlatforms = new MovingPlatformConfig[] {};

    spikes = new SpikeClusterConfig[] {};

    // Encloses the area built so far so the player can't fall into the void or walk off-screen.
    // Walls are 5 units thick (not 1) so a fast dash can't tunnel through them in a single
    // physics step. Inner edge stays at (-1,-1)-(39,19); widen these as the level grows further out.
    bounds =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(-6, -6), 50, 6, 0, groundTFP), // bottom, top edge at y=0
          new PlatformConfig(new GridPoint2(-6, 19), 50, 5, 0, groundTFP), // top
          new PlatformConfig(new GridPoint2(-6, -6), 5, 30, 0, groundTFP), // left
          new PlatformConfig(new GridPoint2(39, -6), 5, 30, 0, groundTFP), // right
        };

    floors =
        new PlatformConfig[] {
          // Starting island the player spawns on
          new PlatformConfig(new GridPoint2(0, 0), 22, 2, 0, groundTFP),
        };

    // // No items are placed at the player's start; pickups are spread out further into the level.
    // items = new HashMap<>();
    // items.put(
    //     new GridPoint2(20, 8),
    //     ItemFactory.createHealthPotion(2).getComponent(ItemComponent.class).getItem());
    // items.put(
    //     new GridPoint2(35, 5),
    //     ItemFactory.createRopeArrow(2).getComponent(ItemComponent.class).getItem());
    // items.put(
    //     new GridPoint2(45, 17),
    //     ItemFactory.createHealthPotion(2).getComponent(ItemComponent.class).getItem());
    // items.put(
    //     new GridPoint2(57, 26),
    //     ItemFactory.createFireArrow(3).getComponent(ItemComponent.class).getItem());
    // items.put(
    //     new GridPoint2(67, 26),
    //     ItemFactory.createColdArrow(3).getComponent(ItemComponent.class).getItem());

//     // Example checkpoint set to player spawn position
//     checkpoints =
//         new CheckpointConfig[] {
//           new CheckpointConfig(new GridPoint2(1, 3)),
//           new CheckpointConfig(new GridPoint2(54, 1)),
//           new CheckpointConfig(new GridPoint2(73, 1)),
//         };
}
}
