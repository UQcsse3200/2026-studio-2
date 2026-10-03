package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SandboxGameAreaTest {
  @Test
  void shouldPreloadHookPlatformTextureForGrapplePlatforms() {
    assertTrue(
        Arrays.asList(SandboxGameArea.getSandboxTextures())
            .contains("images/terrain/Others/platform.png"));
  }

  @Test
  void shouldPreloadTutorialFloorTextureForSandboxGround() {
    assertPreloadsTexture("images/terrain/Others/platform.png");
  }

  @Test
  void shouldPreloadPlayerStatsDisplayTextures() {
    assertPreloadsTexture("images/health/red_heart.png");
    assertPreloadsTexture("images/health/PixelArt_HeartBack.png");
    assertPreloadsTexture("images/health/Damaged_heart.png");
    assertPreloadsTexture("images/health/Last_Health.png");
  }

  private static void assertPreloadsTexture(String texturePath) {
    assertTrue(Arrays.asList(SandboxGameArea.getSandboxTextures()).contains(texturePath));
  }
}
