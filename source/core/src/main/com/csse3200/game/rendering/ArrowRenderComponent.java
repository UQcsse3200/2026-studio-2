package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.services.ServiceLocator;

/** Renders a standard arrow as a small shaft aligned with its travel direction. */
public class ArrowRenderComponent extends RenderComponent {
  private final Texture texture;
  private ArrowProjectileComponent projectile;

  public ArrowRenderComponent(String texturePath) {
    this.texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
  }

  @Override
  public void create() {
    super.create();
    projectile = entity.getComponent(ArrowProjectileComponent.class);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();

    float rotationDegrees = projectile.getDirection().angleDeg();

    batch.draw(
        texture,
        position.x,
        position.y,
        scale.x / 2f,
        scale.y / 2f,
        scale.x,
        scale.y,
        1f,
        1f,
        rotationDegrees,
        0,
        0,
        texture.getWidth(),
        texture.getHeight(),
        false,
        false);
  }

  @Override
  public void dispose() {
    super.dispose();
  }
}
