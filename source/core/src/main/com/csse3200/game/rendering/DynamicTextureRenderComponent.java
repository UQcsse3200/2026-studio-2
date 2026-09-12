package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

public class DynamicTextureRenderComponent extends RenderComponent {
  private Texture texture;

  public DynamicTextureRenderComponent(String texturePath) {
    setTexture(texturePath);
  }

  public DynamicTextureRenderComponent(Texture texture) {
    this.texture = texture;
  }

  public void setTexture(String texturePath) {
    texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
  }

  public void setTexture(Texture texture) {
    this.texture = texture;
  }

  public void scaleEntity() {
    entity.setScale(1f, (float) texture.getHeight() / texture.getWidth());
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();
    batch.draw(texture, position.x, position.y, scale.x, scale.y);
  }
}
