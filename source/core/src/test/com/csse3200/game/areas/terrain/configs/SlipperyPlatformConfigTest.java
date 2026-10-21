package com.csse3200.game.areas.terrain.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;

class SlipperyPlatformConfigTest {

  @Test
  void constructorStoresPlatformProperties() {
    GridPoint2 position = new GridPoint2(3, 5);
    SlipperyPlatformConfig config =
        new SlipperyPlatformConfig(position, 4, 2, 15, "platform.png", 3.5f, 0.2f);

    assertSame(position, config.position);
    assertEquals(4, config.width);
    assertEquals(2, config.height);
    assertEquals(15, config.grappleSides);
    assertEquals("platform.png", config.textureFilepath);
  }

  @Test
  void getMaxGrappleTimeReturnsConfiguredValue() {
    SlipperyPlatformConfig config =
        new SlipperyPlatformConfig(new GridPoint2(1, 2), 3, 1, 15, "platform.png", 5.0f, 0.3f);

    assertEquals(5.0f, config.getMaxGrappleTime());
  }

  @Test
  void getSlipperinessReturnsConfiguredValue() {
    SlipperyPlatformConfig config =
        new SlipperyPlatformConfig(new GridPoint2(1, 2), 3, 1, 15, "platform.png", 5.0f, 0.3f);

    assertEquals(0.3f, config.getSlipperiness());
  }

  @Test
  void supportsZeroValues() {
    SlipperyPlatformConfig config =
        new SlipperyPlatformConfig(new GridPoint2(0, 0), 1, 1, 0, "platform.png", 0f, 0f);

    assertEquals(0f, config.getMaxGrappleTime());
    assertEquals(0f, config.getSlipperiness());
  }

  @Test
  void supportsNegativeValues() {
    SlipperyPlatformConfig config =
        new SlipperyPlatformConfig(new GridPoint2(-2, -3), 1, 1, 0, "platform.png", -1f, -0.5f);

    assertEquals(-1f, config.getMaxGrappleTime());
    assertEquals(-0.5f, config.getSlipperiness());
  }
}
