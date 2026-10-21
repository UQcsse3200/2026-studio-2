package com.csse3200.game.areas.terrain.configs;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

class MovingPlatformConfigTest {

  @Test
  void getActivateIdsReturnsConfiguredIds() {
    String[] activateIds = {"trigger1", "trigger2"};

    MovingPlatformConfig config =
        new MovingPlatformConfig(
            new PlatformConfig(new GridPoint2(2, 3), 4, 2, 15, "platform.png"),
            new Vector2(2, 3),
            new Vector2(8, 3),
            new Vector2(1, 0),
            activateIds);

    assertSame(activateIds, config.getActivateIds());
  }
}
