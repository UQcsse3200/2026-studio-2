package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SandboxGameAreaTest {
  @Test
  void shouldPreloadHookPlatformTextureForGrapplePlatforms() {
    assertTrue(
        Arrays.asList(SandboxGameArea.getSandboxTextures()).contains("images/hook_platform.png"));
  }

  @Test
  void shouldPreloadTutorialFloorTextureForSandboxGround() {
    assertPreloadsTexture("images/Tile_2.png");
  }

  @Test
  void shouldPreloadPlayerStatsDisplayTextures() {
    assertPreloadsTexture("images/red_heart.png");
    assertPreloadsTexture("images/PixelArt_HeartBack.png");
    assertPreloadsTexture("images/Damaged_heart.png");
    assertPreloadsTexture("images/Last_Health.png");
  }

  private static void assertPreloadsTexture(String texturePath) {
    assertTrue(Arrays.asList(SandboxGameArea.getSandboxTextures()).contains(texturePath));
  }
}
