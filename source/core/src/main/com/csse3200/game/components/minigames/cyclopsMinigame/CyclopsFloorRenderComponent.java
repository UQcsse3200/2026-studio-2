package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.rendering.RenderComponent;

/**
 * Draws the floor's ground strip once, repeated horizontally across the room below the entity's
 * position (its top-left), then fills the rest of the way down with a one-pixel-high region of the
 * same texture. Drawn on the lowest render layer so it sits beneath everything else.
 */
public class CyclopsFloorRenderComponent extends RenderComponent {
  private final TextureRegion strip;
  private final TextureRegion fill;
  private final float tileWidth;
  private final float stripHeight;
  private final float roomWidth;
  private final float depth;

  public CyclopsFloorRenderComponent(
      TextureRegion strip,
      TextureRegion fill,
      float tileWidth,
      float stripHeight,
      float roomWidth,
      float depth) {
    this.strip = strip;
    this.fill = fill;
    this.tileWidth = tileWidth;
    this.stripHeight = stripHeight;
    this.roomWidth = roomWidth;
    this.depth = depth;
  }

  @Override
  public int getLayer() {
    return 0;
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 top = entity.getPosition();
    float stripBottom = top.y - stripHeight;
    for (float x = top.x; x < top.x + roomWidth; x += tileWidth) {
      batch.draw(strip, x, stripBottom, tileWidth, stripHeight);
    }
    float fillBottom = top.y - depth;
    batch.draw(fill, top.x, fillBottom, roomWidth, stripBottom - fillBottom);
  }
}
