package com.csse3200.game.components.mainmenu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.ui.UiTestSupport;
import org.junit.jupiter.api.Test;

class MenuBackgroundTest extends UiTestSupport {
  @Test
  void shouldCenterAnOversizedBackdropUsingTheManagedMenuTexture() {
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(64);
    when(texture.getHeight()).thenReturn(64);
    when(resources.getAsset("images/backgrounds/main_menu_bg.png", Texture.class))
        .thenReturn(texture);

    Image background = MenuBackground.create(1600, 900);

    assertEquals(1648f, background.getWidth(), 0.001f);
    assertEquals(927f, background.getHeight(), 0.001f);
    assertEquals(-24f, background.getX(), 0.001f);
    assertEquals(-13.5f, background.getY(), 0.001f);
    TextureRegionDrawable drawable = (TextureRegionDrawable) background.getDrawable();
    assertSame(texture, drawable.getRegion().getTexture());
  }

  @Test
  void shouldRepeatTheShakeWithoutDriftingFromTheCenteredPosition() {
    Image background = MenuBackground.create(1600, 900);

    for (int cycle = 0; cycle < 2; cycle++) {
      background.act(0.25f);
      // libGDX approximates sine with a lookup table; allow a hundredth of a pixel.
      assertEquals(-22.241827f, background.getX(), 0.01f);
      assertEquals(-13.5f, background.getY(), 0.001f);
      background.act(0.75f);
      assertEquals(-12.0f, background.getX(), 0.001f);
      assertEquals(-13.5f, background.getY(), 0.001f);
      background.act(1f);
      assertEquals(-24f, background.getX(), 0.001f);
      assertEquals(-1.5f, background.getY(), 0.001f);
      background.act(1f);
      assertEquals(-36.0f, background.getX(), 0.001f);
      assertEquals(-13.5f, background.getY(), 0.001f);
      background.act(1f);
      assertEquals(-24f, background.getX(), 0.001f);
      assertEquals(-13.5f, background.getY(), 0.001f);
    }
  }
}
