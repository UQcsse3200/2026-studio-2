package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.services.ServiceLocator;

/**
 * Renders a projectile aligned with its flight trajectory. Standard, fire and cold arrows draw the
 * {@code images/arrow.png} sprite; the grapple line draws a plain grey bolt.
 *
 * <p>The drawn size is independent of the entity's (small) physics scale - change {@link
 * #BASE_RENDER_SIZE} / {@link #STANDARD_SIZE_MULTIPLIER} or call {@link #setRenderSize(float)} to
 * make arrows bigger or smaller.
 */
public class ArrowRenderComponent extends RenderComponent {

  /** Base world-space length of the longest edge of the drawn sprite. */
  private static final float BASE_RENDER_SIZE = 2.0f;

  /** The normal arrow is drawn this many times larger than the base size. */
  private static final float STANDARD_SIZE_MULTIPLIER = 1.0f;

  /** Shared 1x1 white pixel, only used for the grapple's plain bolt. */
  private static Texture pixelTexture;

  private final ArrowType arrowType;
  private float renderSize;
  private ArrowProjectileComponent projectile;
  private Texture arrowTexture;

  public ArrowRenderComponent() {
    this(ArrowType.STANDARD);
  }

  public ArrowRenderComponent(ArrowType arrowType) {
    this.arrowType = arrowType != null ? arrowType : ArrowType.STANDARD;
    this.renderSize = defaultRenderSize(this.arrowType);
  }

  /** Normal arrows render four times larger than fire, cold and grapple projectiles. */
  private static float defaultRenderSize(ArrowType arrowType) {
    return arrowType == ArrowType.STANDARD
        ? BASE_RENDER_SIZE * STANDARD_SIZE_MULTIPLIER
        : BASE_RENDER_SIZE;
  }

  /**
   * Sets how large the arrow is drawn, in world units, independent of the entity's physics scale.
   *
   * @param renderSize length of the longest sprite edge in world units
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

    Vector2 dir =
        (projectile != null) ? projectile.getCurrentDirection() : new Vector2(1f, 0f);
    float rotationDeg = dir.angleDeg();

    // Size the sprite from its own aspect ratio so it is never squished.
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
        x, y,
        width / 2f, height / 2f, // rotate about the sprite centre
        width, height,
        1f, 1f,
        rotationDeg,
        0, 0, texture.getWidth(), texture.getHeight(),
        false, false);

    batch.setColor(Color.WHITE);
  }

  /**
   * @return the arrow sprite, or the shared pixel for the grapple; null if the arrow texture is not
   *     loaded yet
   */
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
