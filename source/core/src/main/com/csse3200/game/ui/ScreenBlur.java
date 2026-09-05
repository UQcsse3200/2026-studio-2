package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

/** Blurs the frame currently on screen*/
public final class ScreenBlur {
  private static final int DOWNSCALE = 8;

  private ScreenBlur() {}

  /**
   * Captures the frame currently on screen and shrinks it. Drawing the result back at full size
   * relies on bilinear filtering to blur it.
   *
   * <p> Must be called after the frame has been rendered, before the buffers are swapped.
   *
   * @return the blurred frame, flipped to match scene2d's coordinates. The caller owns the
   *     texture behind it and must dispose it.
   * 
   * 
   */
  public static TextureRegion capture() {
    int width = Gdx.graphics.getBackBufferWidth();
    int height = Gdx.graphics.getBackBufferHeight();
    Pixmap frame = ScreenUtils.getFrameBufferPixmap(0, 0, width, height);

    Pixmap small =
        new Pixmap(
            Math.max(1, width / DOWNSCALE), Math.max(1, height / DOWNSCALE), frame.getFormat());
    small.setFilter(Pixmap.Filter.BiLinear);
    small.drawPixmap(frame, 0, 0, width, height, 0, 0, small.getWidth(), small.getHeight());
    frame.dispose();

    Texture texture = new Texture(small);
    texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    small.dispose();

    TextureRegion blurred = new TextureRegion(texture);
    blurred.flip(false, true);
    return blurred;
  }
}