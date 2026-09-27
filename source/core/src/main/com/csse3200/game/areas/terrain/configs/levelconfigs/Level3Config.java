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

    playerSpawn = new GridPoint2(9, 6);
    winConditionSpawn = new GridPoint2(33, 8);

    platforms =
        new PlatformConfig[] {
          new PlatformConfig(new GridPoint2(9, 5), 3, 1, 0, platformTFP), // P1
          new PlatformConfig(new GridPoint2(13, 7), 3, 1, 0, platformTFP), // P2
          new PlatformConfig(new GridPoint2(6, 10), 3, 1, 3, platformTFP), // P3
          new PlatformConfig(new GridPoint2(17, 10), 3, 1, 0, platformTFP), // P4
          new PlatformConfig(new GridPoint2(12, 12), 3, 1, 0, platformTFP), // P5
          new PlatformConfig(new GridPoint2(16, 15), 3, 1, 0, platformTFP), // P6
          new PlatformConfig(new GridPoint2(6, 18), 3, 1, 0, platformTFP), // P7
          new PlatformConfig(new GridPoint2(12, 20), 3, 1, 0, platformTFP), // SP1 TODO
          new PlatformConfig(new GridPoint2(3, 21), 3, 1, 0, platformTFP), // P10
        };
        
    floors =
        new PlatformConfig[] {
             new PlatformConfig(new GridPoint2(20, 19), 1, 3, 1, groundTFP), // P9
             new PlatformConfig(new GridPoint2(0, 27), 12, 1, 3, groundTFP), // P9
             new PlatformConfig(new GridPoint2(15, 27), 12, 1, 3, groundTFP), // P9
                 };

     movingPlatforms =
         new MovingPlatformConfig[] {
          new MovingPlatformConfig( // MP1
              new GridPoint2(3, 12),
               1,
               3,
               1,
               movingPlatformTFP,
               new Vector2(3, 12),
               new Vector2(3, 19),
               new Vector2(0, 3),
               new String[] {}), // MP1
          new MovingPlatformConfig( // MP1
              new GridPoint2(12, 24),
               3,
               1,
               1,
               movingPlatformTFP,
               new Vector2(12, 24),
               new Vector2(19, 24),
               new Vector2(3, 0),
               new String[] {}), // MP1
         };

         
         triggerablePlatforms =
         new TriggerablePlatformConfig[] {
           //new TriggerablePlatformConfig(new GridPoint2(12, 27),3,1,2,triggerablePlatformTFP,new String[] {}, true), // TP1 todo uncomment
            };
            
            ledges =
            new PlatformConfig[] {
              new PlatformConfig(new GridPoint2(9, 30), 3, 1, 0, ledgesTFP), // L1
              new PlatformConfig(new GridPoint2(20, 29), 3, 1, 0, ledgesTFP), // L1
            };
            // triggerButtons =
            //     new TriggerButtonConfig[] {
              //       new TriggerButtonConfig(
                //           new GridPoint2(25, 42), 180f, false, new String[] {"triggerWheelSpinPlatform"}), // B1
                //     };
                
                // items = new HashMap<>(Map.of(new GridPoint2(40, 29), new Arrow(ItemType.STANDARD_ARROW, 99)));
                
                
                // spikes =
                //     new SpikeClusterConfig[] {
                  //       new SpikeClusterConfig(0, 11, 37, 37, 0f, false), // SC1
                  //     };
                  
                  // ballTraps =
                  //     new SpikyBallTrapConfig[] {
                    //       new SpikyBallTrapConfig(
                      //           new GridPoint2(7, 19),
                      //           180f,
                      //           new String[] {},
                      //           1.25f,
                      //           true,
                      //           SpawnerComponent.ACTIVATION_MODE.TOGGLE), // SBT1
                      //     };
                      
                      // checkpoints =
                      //     new CheckpointConfig[] {
                        //       new CheckpointConfig(new GridPoint2(0, 41)),
                        //     };
                        // crumblingPlatforms =
                        //     new CrumblingPlatformConfig[] {
                        //       new CrumblingPlatformConfig(
                        //           new GridPoint2(16, 8), 3, 1, 0, crumblingPlatformTFP, 1.25f, 0.75f, 3f), // CP1
                        //     };
                      }
                    }
                    