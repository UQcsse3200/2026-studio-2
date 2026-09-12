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
    assertEquals(78, atlas.findRegion("default").getRegionWidth());
    assertEquals(204, atlas.findRegion("default").getRegionHeight());
    assertEquals(8, atlas.findRegions("idle").size);
    assertEquals(8, atlas.findRegions("walk").size);
    assertEquals(8, atlas.findRegions("sprint").size);
    assertEquals(16, atlas.findRegions("jump").size);
    assertEquals(7, atlas.findRegions("hurt").size);
    for (String animation : new String[] {"idle", "walk", "sprint", "jump", "hurt"}) {
      assertEquals(204, atlas.findRegion(animation).getRegionHeight(), animation);
    }

    atlas.dispose();
  }
}
