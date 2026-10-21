package com.csse3200.game.areas.terrain.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.components.level.SpawnerComponent;
import org.junit.jupiter.api.Test;

class SpikyBallTrapConfigTest {

  @Test
  void constructorStoresTrapConfiguration() {
    GridPoint2 position = new GridPoint2(4, 7);
    String[] ids = {"trigger1", "trigger2"};
    SpawnerComponent.ACTIVATION_MODE mode = SpawnerComponent.ACTIVATION_MODE.values()[0];

    SpikyBallTrapConfig config = new SpikyBallTrapConfig(position, 90f, ids, 2.5f, false, mode);

    assertSame(position, config.getPosition());
    assertEquals(90f, config.getRotation());
    assertSame(ids, config.getIds());
    assertEquals(2.5f, config.getSpawnInterval());
    assertFalse(config.getInitialState());
    assertSame(mode, config.getMode());
  }
}
