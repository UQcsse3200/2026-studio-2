package com.csse3200.game.areas.terrain.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.areas.terrain.configs.levelconfigs.Level1Config;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level2Config;
import com.csse3200.game.areas.terrain.configs.levelconfigs.Level3Config;
import com.csse3200.game.components.level.CheckpointComponent;
import com.csse3200.game.entities.Entity;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CheckpointLayoutTest {
  private static final int CHECKPOINTS_PER_LEVEL = 5;
  // The widest a checkpoint statue can be (the Level 2 art is square), to keep it on its platform.
  private static final float STATUE_WIDTH = CheckpointComponent.ATLAS_HEIGHT;

  @Test
  void everyLevelHasFiveDistinctCheckpoints() {
    for (LevelConfig config : levels()) {
      assertEquals(CHECKPOINTS_PER_LEVEL, config.checkpoints.length);
      Set<String> positions = new HashSet<>();
      for (CheckpointConfig checkpoint : config.checkpoints) {
        positions.add(checkpoint.getPosition().toString());
      }
      assertEquals(CHECKPOINTS_PER_LEVEL, positions.size());
    }
  }

  @Test
  void everyCheckpointStandsOnAFixedPlatformAndHasHeadroom() {
    for (LevelConfig config : levels()) {
      for (CheckpointConfig checkpoint : config.checkpoints) {
        int x = checkpoint.getPosition().x;
        int y = checkpoint.getPosition().y;
        String where =
            config.getClass().getSimpleName() + " checkpoint at " + checkpoint.getPosition();

        assertTrue(isSolidTopAt(config, x + 0.1f, y), where + " has no platform under its left");
        assertTrue(
            isSolidTopAt(config, x + STATUE_WIDTH - 0.1f, y),
            where + " has no platform under its right");
        assertTrue(
            !isInsideSolid(config, x, y, STATUE_WIDTH, CheckpointComponent.ATLAS_HEIGHT),
            where + " is inside a platform");
      }
    }
  }

  private static LevelConfig[] levels() {
    return new LevelConfig[] {
      new Level1Config(), new Level2Config(new Entity()), new Level3Config(new Entity())
    };
  }

  /** True if some fixed platform has its top surface at height y and covers the given x. */
  private static boolean isSolidTopAt(LevelConfig config, float x, int y) {
    for (PlatformConfig platform : fixedPlatforms(config)) {
      if (platform.position.y + platform.height == y
          && platform.position.x <= x
          && x <= platform.position.x + platform.width) {
        return true;
      }
    }
    return false;
  }

  /** True if the box (x, y, width, height) overlaps any fixed platform. */
  private static boolean isInsideSolid(
      LevelConfig config, float x, float y, float width, float height) {
    for (PlatformConfig platform : fixedPlatforms(config)) {
      if (platform.position.x < x + width
          && platform.position.x + platform.width > x
          && platform.position.y < y + height
          && platform.position.y + platform.height > y) {
        return true;
      }
    }
    return false;
  }

  /** Platforms that never move, crumble or slip: the only ones a checkpoint may stand on. */
  private static Iterable<PlatformConfig> fixedPlatforms(LevelConfig config) {
    Set<PlatformConfig> all = new HashSet<>();
    for (PlatformConfig[] group :
        new PlatformConfig[][] {config.platforms, config.floors, config.ledges, config.bounds}) {
      if (group != null) {
        for (PlatformConfig platform : group) {
          // Moving, crumbling, triggerable and slippery platforms are subclasses in the lists.
          if (platform.getClass() == PlatformConfig.class) {
            all.add(platform);
          }
        }
      }
    }
    return all;
  }
}
