package com.csse3200.game.areas.terrain.configs.levelconfigs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class Level3ConfigTest {

  @Test
  void wiresATipToEveryCheckpointUpTheClimb() {
    Level3Config config = new Level3Config(new Entity(), null);

    assertEquals("tut10", config.getTutorialForCheckpoint(new GridPoint2(10, 6)));
    assertEquals("tut11", config.getTutorialForCheckpoint(new GridPoint2(16, 37)));
    assertEquals("tut12", config.getTutorialForCheckpoint(new GridPoint2(16, 68)));
    assertEquals("tut13", config.getTutorialForCheckpoint(new GridPoint2(13, 85)));
    assertEquals("tut14", config.getTutorialForCheckpoint(new GridPoint2(13, 98)));
  }

  @Test
  void everyCheckpointHasATip() {
    Level3Config config = new Level3Config(new Entity(), null);

    for (GridPoint2 position : checkpointPositions()) {
      assertNotNull(
          config.getTutorialForCheckpoint(position),
          "checkpoint at " + position + " wires no tutorial");
    }
  }

  /** The five checkpoints the level declares, in climb order. */
  private static List<GridPoint2> checkpointPositions() {
    return List.of(
        new GridPoint2(10, 6),
        new GridPoint2(16, 37),
        new GridPoint2(16, 68),
        new GridPoint2(13, 85),
        new GridPoint2(13, 98));
  }

  @Test
  void unknownPositionsWireNoTutorial() {
    Level3Config config = new Level3Config(new Entity(), null);

    assertNull(config.getTutorialForCheckpoint(new GridPoint2(99, 99)));
  }

  @Test
  void level3WiresNoRegionTutorials() {
    assertTrue(new Level3Config(new Entity(), null).getRegionTutorials().isEmpty());
  }
}
