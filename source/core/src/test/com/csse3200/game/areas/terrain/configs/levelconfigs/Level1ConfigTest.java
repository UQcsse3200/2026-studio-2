package com.csse3200.game.areas.terrain.configs.levelconfigs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.configs.LevelConfig;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class Level1ConfigTest {

  @Test
  void wiresTutorialsToTheFirstTwoCheckpoints() {
    Level1Config config = new Level1Config();

    assertEquals("tut5", config.getTutorialForCheckpoint(new GridPoint2(16, 5)));
    assertEquals("tut6", config.getTutorialForCheckpoint(new GridPoint2(19, 17)));
  }

  @Test
  void laterCheckpointsAndUnknownPositionsWireNoTutorial() {
    Level1Config config = new Level1Config();

    assertNull(config.getTutorialForCheckpoint(new GridPoint2(44, 17)));
    assertNull(config.getTutorialForCheckpoint(new GridPoint2(99, 99)));
  }

  @Test
  void levelsWithoutTutorialsWireNoneByDefault() {
    assertNull(new LevelConfig().getTutorialForCheckpoint(new GridPoint2(16, 5)));
  }
}
