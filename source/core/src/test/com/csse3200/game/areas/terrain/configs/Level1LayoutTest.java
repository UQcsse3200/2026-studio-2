package com.csse3200.game.areas.terrain.configs;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Rectangle;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level1Config;
import org.junit.jupiter.api.Test;

class Level1LayoutTest {
  @Test
  void fullWidthLiftsClearOriginalTerrainThroughoutTheirTravel() {
    LevelConfig config = new Level1Config();
    for (MovingPlatformConfig lift : config.movingPlatforms) {
      assertEquals(3, lift.width);
      assertEquals(1, lift.height);
      Rectangle travel =
          new Rectangle(
              Math.min(lift.firstTarget.x, lift.secondTarget.x),
              Math.min(lift.firstTarget.y, lift.secondTarget.y),
              Math.abs(lift.firstTarget.x - lift.secondTarget.x) + lift.width,
              Math.abs(lift.firstTarget.y - lift.secondTarget.y) + lift.height);
      for (PlatformConfig floor : config.floors) {
        assertFalse(
            travel.overlaps(
                new Rectangle(floor.position.x, floor.position.y, floor.width, floor.height)),
            "Lift at " + lift.position + " intersects floor at " + floor.position);
      }
    }
  }

  @Test
  void upperCrossingLeavesStandingHeadroomUnderOriginalCeiling() {
    LevelConfig config = new Level1Config();
    for (PlatformConfig platform : config.platforms) {
      assertTrue(platform.position.y + platform.height + 2 <= 27);
    }
    assertEquals(2, config.checkpoints.length);
    assertTrue(config.checkpoints[1].getPosition().x - config.checkpoints[0].getPosition().x >= 35);
  }
}
