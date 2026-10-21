package com.csse3200.game.areas.terrain.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.csse3200.game.areas.terrain.configs.levelconfigs.Level3Config;
import org.junit.jupiter.api.Test;

class Level3ConfigTest {

  @Test
  void risingWaterSpawnsBelowLevel() {
    Level3Config config = new Level3Config(null, null);

    assertEquals(0, config.getRisingWaterSpawn().x);
    assertEquals(-10, config.getRisingWaterSpawn().y);
  }

  @Test
  void risingWaterHasExpectedSpeed() {
    Level3Config config = new Level3Config(null, null);

    assertEquals(0.3f, config.getWaterSpeed());
  }
}
