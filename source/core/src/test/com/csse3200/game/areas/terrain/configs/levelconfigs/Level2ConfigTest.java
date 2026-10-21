package com.csse3200.game.areas.terrain.configs.levelconfigs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.LevelConfig;
import com.csse3200.game.areas.terrain.configs.RegionTutorialConfig;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class Level2ConfigTest {

  @Test
  void wiresTutorialsToTheFirstFourCheckpoints() {
    Level2Config config = new Level2Config(new Entity());

    assertEquals("tut3", config.getTutorialForCheckpoint(new GridPoint2(8, 40)));
    assertEquals("tut7", config.getTutorialForCheckpoint(new GridPoint2(11, 24)));
    assertEquals("tut8", config.getTutorialForCheckpoint(new GridPoint2(6, 9)));
    assertEquals("tut9", config.getTutorialForCheckpoint(new GridPoint2(38, 28)));
  }

  @Test
  void theLastCheckpointAndUnknownPositionsWireNoTutorial() {
    Level2Config config = new Level2Config(new Entity());

    assertNull(config.getTutorialForCheckpoint(new GridPoint2(44, 8)));
    assertNull(config.getTutorialForCheckpoint(new GridPoint2(99, 99)));
  }

  @Test
  void wiresTheGrappleTutorialToTheArenaLedge() {
    List<RegionTutorialConfig> regions = new Level2Config(new Entity()).getRegionTutorials();

    assertEquals(1, regions.size());
    RegionTutorialConfig region = regions.get(0);
    assertEquals("tut2", region.tutorial);
    assertTrue(region.contains(new Vector2(30f, 23f))); // standing on the lone ledge
    assertTrue(region.contains(new Vector2(28.5f, 22.6f))); // half-tile resting-error margins
    assertFalse(region.contains(new Vector2(30f, 22f))); // underneath the ledge never triggers
    assertFalse(region.contains(new Vector2(30f, 24f))); // above the region
    assertFalse(region.contains(new Vector2(27f, 23f))); // the gap beside it
  }

  @Test
  void levelsWithoutTutorialsWireNoneByDefault() {
    assertNull(new LevelConfig().getTutorialForCheckpoint(new GridPoint2(8, 40)));
    assertTrue(new LevelConfig().getRegionTutorials().isEmpty());
  }
}
