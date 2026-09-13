package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/** Render a static texture. */
public class TextureRenderComponent extends RenderComponent {
  private final Texture texture;
  private Color tint = Color.WHITE;

  /**
   * @param texturePath Internal path of static texture to render. Will be scaled to the entity's
   *     scale.
   */
  public TextureRenderComponent(String texturePath) {
    this(ServiceLocator.getResourceService().getAsset(texturePath, Texture.class));
  }

  /**
   * @param texture Static texture to render. Will be scaled to the entity's scale.
   */
  public TextureRenderComponent(Texture texture) {
    this.texture = texture;
  }

  /**
   * Tints the texture when drawn. White (the default) leaves it unchanged.
   *
   * @param tint colour to multiply the texture by
   * @return self
   */
  public TextureRenderComponent setTint(Color tint) {
    this.tint = tint;
    return this;
  }

  /** Scale the entity to a width of 1 and a height matching the texture's ratio */
  public void scaleEntity() {
    entity.setScale(1f, (float) texture.getHeight() / texture.getWidth());
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();
    Color previous = batch.getColor().cpy();
    batch.setColor(tint);
    batch.draw(texture, position.x, position.y, scale.x, scale.y);
    batch.setColor(previous);
  }
}
