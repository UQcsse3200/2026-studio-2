package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Verifies images/player.atlas parses correctly and exposes the regions the player uses. */
@ExtendWith(GameExtension.class)
class PlayerAtlasTest {
  private TextureAtlas atlas;

  @BeforeEach
  void beforeEach() {
    atlas = new TextureAtlas(Gdx.files.internal("images/player.atlas"));
  }

  @AfterEach
  void afterEach() {
    atlas.dispose();
  }

  @Test
  void shouldExposeIdleAnimation() {
    assertEquals(16, atlas.findRegions("idle").size);
  }

  @Test
  void shouldExposeWalkAnimation() {
    assertEquals(8, atlas.findRegions("walk").size);
  }

  @Test
  void shouldExposeSprintAnimation() {
    assertEquals(12, atlas.findRegions("sprint").size);
  }

  @Test
  void shouldExposeJumpAnimation() {
    assertEquals(11, atlas.findRegions("jump").size);
  }

  @Test
  void shouldExposeHurtAnimation() {
    assertEquals(21, atlas.findRegions("hurt").size);
  }

  @Test
  void shouldExposeDeathAnimation() {
    assertEquals(16, atlas.findRegions("death").size);
  }

  @Test
  void shouldExposeSleepAnimation() {
    assertEquals(16, atlas.findRegions("sleep").size);
  }
}
