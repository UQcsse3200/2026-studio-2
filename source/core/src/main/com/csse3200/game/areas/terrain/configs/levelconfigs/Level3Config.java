package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.*;
// import com.csse3200.game.components.item.weapons.RopeArr;
// import com.csse3200.game.components.item.weapons.StandardArr;
import com.csse3200.game.components.item.*;
import com.csse3200.game.components.item.weapons.bow.arrow.*;
import com.csse3200.game.components.level.SpawnerComponent;
import java.util.HashMap;
import java.util.Map;

public class Level3Config extends LevelConfig {

  public Level3Config() {
    // Textures
    // TFP = Texture File Path
    platformTFP = "images/Platform_level-3.png";
    movingPlatformTFP = "images/Platform_level-3.png";
    crumblingPlatformTFP = "images/Platform-crumbling-level-3.png";
    triggerablePlatformTFP = "images/Platform_level-3.png";
    ledgesTFP = "images/Platform_level-3.png";
    groundTFP = "images/tile-level3.png";

    playerSpawn = new GridPoint2(0, 42);
    winConditionSpawn = new GridPoint2(33, 8);
    risingWaterSpawn = new GridPoint2(0, 40);

    platforms =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(0, 40), 3, 1, 3, platformTFP), // P1
        };

    movingPlatforms =
        new MovingPlatformConfig[] {
          new MovingPlatformConfig(
              new GridPoint2(0, 11),
              3,
              1,
              1,
              movingPlatformTFP,
              new Vector2(0, 11),
              new Vector2(6, 11),
              new Vector2(3, 0),
              new String[] {}), // MP1
        };

    crumblingPlatforms =
        new CrumblingPlatformConfig[] {
          new CrumblingPlatformConfig(
              new GridPoint2(16, 8), 3, 1, 0, crumblingPlatformTFP, 1.25f, 0.75f, 3f), // CP1
        };

    triggerablePlatforms =
        new TriggerablePlatformConfig[] {
          new TriggerablePlatformConfig(
              new GridPoint2(25, 25),
              3,
              1,
              2,
              triggerablePlatformTFP,
              new String[] {}, // should be enemyArenaComplete
              true), // P7
        };

    floors =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(0, 35), 13, 2, 3, groundTFP), // G1
        };

    triggerButtons =
        new TriggerButtonConfig[] {
          new TriggerButtonConfig(
              new GridPoint2(25, 42), 180f, false, new String[] {"triggerWheelSpinPlatform"}), // B1
        };

    items = new HashMap<>(Map.of(new GridPoint2(40, 29), new Arrow(ItemType.STANDARD_ARROW, 99)));

    ledges =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(29, 15), 3, 1, 0, ledgesTFP), // L1
        };

    spikes =
        new SpikeClusterConfig[] {
          new SpikeClusterConfig(0, 11, 37, 37, 0f, false), // SC1
        };

    ballTraps =
        new SpikyBallTrapConfig[] {
          new SpikyBallTrapConfig(
              new GridPoint2(7, 19),
              180f,
              new String[] {},
              1.25f,
              true,
              SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT1
        };

    checkpoints =
        new CheckpointConfig[] {
          new CheckpointConfig(new GridPoint2(0, 41)),
        };
  }
}
