package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.level.PlatformGrappleComponent;
import com.csse3200.game.services.ServiceLocator;

public class GrappleSideRenderComponent extends RenderComponent {

  private static final String GRAPPLE_TEXTURE = "images/terrain/grapple_tile.png";

  /** Matches the tile size used for the normal ground texture (see ObstacleFactory#createFloor). */
  private static final float STRIP_THICKNESS = 0.75f;

  private TextureRegion textureRegion;
  private PlatformGrappleComponent grappleComponent;

  @Override
  public void create() {
    super.create();
    grappleComponent = entity.getComponent(PlatformGrappleComponent.class);

    Texture texture =
        ServiceLocator.getResourceService().getAsset(GRAPPLE_TEXTURE, Texture.class);
    texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    textureRegion = new TextureRegion(texture);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (grappleComponent == null) {
      return;
    }

    Vector2 pos = entity.getPosition();
    Vector2 scale = entity.getScale();

    float left = pos.x;
    float right = pos.x + scale.x;
    float bottom = pos.y;
    float top = pos.y + scale.y;

    // Left/right strips mirror each other so the art faces into the platform on both sides.
    if (grappleComponent.isSideGrappleable(PlatformGrappleComponent.LEFT_SIDE)) {
      drawVerticalStrip(batch, left, bottom, top - bottom, false);
    }
    if (grappleComponent.isSideGrappleable(PlatformGrappleComponent.RIGHT_SIDE)) {
      drawVerticalStrip(batch, right - STRIP_THICKNESS, bottom, top - bottom, true);
    }

    // Top/bottom strips rotate in opposite directions for the same reason.
    if (grappleComponent.isSideGrappleable(PlatformGrappleComponent.TOP_SIDE)) {
      drawHorizontalStrip(batch, left, top - STRIP_THICKNESS, right - left, 90f);
    }
    if (grappleComponent.isSideGrappleable(PlatformGrappleComponent.BOTTOM_SIDE)) {
      drawHorizontalStrip(batch, left, bottom, right - left, -90f);
    }
  }

  /**
   * Draws the grapple texture tiled along a vertical strip, kept flush inside the platform's
   * boundary rather than straddling the edge.
   *
   * @param mirror flips the art horizontally, used so the left and right edges face each other
   *     instead of both facing the same way
   */
  private void drawVerticalStrip(SpriteBatch batch, float x, float y, float length, boolean mirror) {
    textureRegion.setU(0f);
    textureRegion.setV(0f);
    textureRegion.setU2(1f);
    textureRegion.setV2(length / STRIP_THICKNESS);
    if (mirror) {
      textureRegion.flip(true, false);
    }
    batch.draw(textureRegion, x, y, STRIP_THICKNESS, length);
  }

  /**
   * Draws the grapple texture tiled along a horizontal strip, rotated from its native vertical
   * orientation so it reads correctly running along a top/bottom edge instead of a side edge.
   *
   * @param rotation +90 for the top edge, -90 for the bottom edge, so the two face away from
   *     each other correctly
   */
  private void drawHorizontalStrip(SpriteBatch batch, float x, float y, float length, float rotation) {
    textureRegion.setU(0f);
    textureRegion.setV(0f);
    textureRegion.setU2(1f);
    textureRegion.setV2(length / STRIP_THICKNESS);

    float centerX = x + length / 2f;
    float centerY = y + STRIP_THICKNESS / 2f;
    float drawX = centerX - STRIP_THICKNESS / 2f;
    float drawY = centerY - length / 2f;

    batch.draw(
        textureRegion,
        drawX,
        drawY,
        STRIP_THICKNESS / 2f,
        length / 2f,
        STRIP_THICKNESS,
        length,
        1f,
        1f,
        rotation);
  }
}
