package com.csse3200.game.rendering.item;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;

/** Draws a projectile's sprite rotated to face its flight direction. */
public class ArrowRenderComponent extends RenderComponent {
  private final ArrowType arrowType;
  private Float renderSize;
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

    Vector2 scale = entity.getScale();
    float width = scale.x;
    float height = scale.y;
    if (renderSize != null) {
      if (texture.getWidth() >= texture.getHeight()) {
        width = renderSize;
        height = renderSize * texture.getHeight() / texture.getWidth();
      } else {
        width = renderSize * texture.getWidth() / texture.getHeight();
        height = renderSize;
      }
    }

    // arrow.png points diagonally up and right, while the elemental sprites point right.
    // Non-uniform sizing changes that diagonal's angle, so account for the displayed dimensions
    // as well as the artwork before aligning its shaft with the velocity.
    float spriteAngle =
        switch (arrowType) {
          case STANDARD, GRAPPLE, POISON ->
              MathUtils.atan2(height, width) * MathUtils.radiansToDegrees;
          default -> 0f;
        };
    float rotationDeg = dir.angleDeg() - spriteAngle;

    Vector2 center = projectile != null ? projectile.getWorldCenter() : entity.getCenterPosition();
    float x = center.x - width / 2f;
    float y = center.y - height / 2f;

    batch.setColor(Color.WHITE);
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
  }

  private Texture resolveTexture() {
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
