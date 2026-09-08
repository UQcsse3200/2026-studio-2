package com.csse3200.game.rendering.item;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;

/** Draws a projectile's sprite rotated to face its flight direction. */
public class ArrowRenderComponent extends RenderComponent {
  private static final float RENDER_SIZE = 2f;

  private static Texture pixelTexture;

  private final ArrowType arrowType;
  private float renderSize = RENDER_SIZE;
  private ArrowProjectileComponent projectile;
  private Texture arrowTexture;

  public ArrowRenderComponent() {
    this(ArrowType.STANDARD);
  }

  public ArrowRenderComponent(ArrowType arrowType) {
    this.arrowType = arrowType != null ? arrowType : ArrowType.STANDARD;
  }

  /**
   * @param renderSize length of the longest sprite edge, in world units
   * @return this component
   */
  public ArrowRenderComponent setRenderSize(float renderSize) {
    this.renderSize = renderSize;
    return this;
  }

  @Override
  public void create() {
    super.create();
    projectile = entity.getComponent(ArrowProjectileComponent.class);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Texture texture = resolveTexture();
    if (texture == null) {
      return;
    }

    Vector2 dir = (projectile != null) ? projectile.getCurrentDirection() : new Vector2(1f, 0f);
    float rotationDeg = dir.angleDeg();

    float width;
    float height;
    if (arrowType == ArrowType.GRAPPLE) {
      width = renderSize;
      height = renderSize * 0.25f;
    } else if (texture.getWidth() >= texture.getHeight()) {
      width = renderSize;
      height = renderSize * texture.getHeight() / texture.getWidth();
    } else {
      width = renderSize * texture.getWidth() / texture.getHeight();
      height = renderSize;
    }

    Vector2 center = entity.getCenterPosition();
    float x = center.x - width / 2f;
    float y = center.y - height / 2f;

    boolean grapple = arrowType == ArrowType.GRAPPLE;
    batch.setColor(grapple ? Color.LIGHT_GRAY : Color.WHITE);
    batch.draw(
        texture,
        x,
        y,
        width / 2f,
        height / 2f,
        width,
        height,
        1f,
        1f,
        rotationDeg,
        0,
        0,
        texture.getWidth(),
        texture.getHeight(),
        false,
        false);
    batch.setColor(Color.WHITE);
  }

  private Texture resolveTexture() {
    if (arrowType == ArrowType.GRAPPLE) {
      if (pixelTexture == null) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixelTexture = new Texture(pixmap);
        pixmap.dispose();
      }
      return pixelTexture;
    }

    if (arrowTexture == null && ServiceLocator.getResourceService() != null) {
      try {
        arrowTexture =
            ServiceLocator.getResourceService().getAsset(arrowType.getTexturePath(), Texture.class);
      } catch (RuntimeException e) {
        return null;
      }
    }
    return arrowTexture;
  }
}
