package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.rendering.RenderComponent;

/**
 * Tiles a texture region across the room below the entity's position, which is the top-left of
 * the floor. Drawn on the lowest render layer so it sits beneath everything else.
 */
public class CyclopsFloorRenderComponent extends RenderComponent {
  private final TextureRegion region;
  private final float tileWidth;
  private final float tileHeight;
  private final float roomWidth;
  private final float depth;

  public CyclopsFloorRenderComponent(
      TextureRegion region, float tileWidth, float tileHeight, float roomWidth, float depth) {
    this.region = region;
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
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
    for (float y = top.y - tileHeight; y > top.y - depth; y -= tileHeight) {
      for (float x = top.x; x < top.x + roomWidth; x += tileWidth) {
        batch.draw(region, x, y, tileWidth, tileHeight);
      }
    }
  }
}
