package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Verifies images/player.atlas parses correctly and exposes the regions the player uses. */
@ExtendWith(GameExtension.class)
class PlayerAtlasTest {
  @Test
  void shouldParseExpectedRegions() {
    TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("images/player.atlas"));

    assertNotNull(atlas.findRegion("default"));
    assertNotNull(atlas.findRegion("idle"));
    // "default" is idle frame 0: every page shares a 204px-tall cell so animations draw at a
    // consistent scale (see AnimationRenderComponent, which scales by the default region width).
    // "default" must be idle frame 0 exactly, since the animator scales every frame off it.
    assertEquals(
        atlas.findRegion("idle").getRegionWidth(), atlas.findRegion("default").getRegionWidth());
    assertEquals(204, atlas.findRegion("default").getRegionHeight());
    assertEquals(8, atlas.findRegions("idle").size);
    assertEquals(8, atlas.findRegions("walk").size);
    assertEquals(6, atlas.findRegions("sprint").size);
    assertEquals(7, atlas.findRegions("jump").size);
    assertEquals(7, atlas.findRegions("hurt").size);
    assertEquals(12, atlas.findRegions("roll").size);
    // Upright poses share the standing cell height; leaning/crouching poses (sprint, roll) are a
    // little shorter and the arms-raised jump apex a little taller, so the character keeps one
    // consistent size across animations.
    for (String animation : new String[] {"idle", "walk", "hurt"}) {
      assertEquals(204, atlas.findRegion(animation).getRegionHeight(), animation);
    }
    assertEquals(188, atlas.findRegion("sprint").getRegionHeight());
    assertEquals(184, atlas.findRegion("roll").getRegionHeight());
    assertEquals(224, atlas.findRegion("jump").getRegionHeight());

    atlas.dispose();
  }
}
