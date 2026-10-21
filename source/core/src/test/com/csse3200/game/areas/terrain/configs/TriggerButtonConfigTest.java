package com.csse3200.game.areas.terrain.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;

class TriggerButtonConfigTest {

  @Test
  void constructorStoresConfiguration() {
    GridPoint2 position = new GridPoint2(3, 5);
    String[] ids = {"trigger1", "trigger2"};

    TriggerButtonConfig config = new TriggerButtonConfig(position, 90f, true, ids);

    assertSame(position, config.getPosition());
    assertEquals(90f, config.getRotation());
    assertTrue(config.getAttached());
    assertSame(ids, config.getIds());
  }
}
