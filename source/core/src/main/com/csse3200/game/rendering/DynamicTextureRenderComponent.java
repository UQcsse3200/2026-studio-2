package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.level.RotatableMapComponent;
import com.csse3200.game.services.ServiceLocator;

public class DynamicTextureRenderComponent extends RenderComponent {
  private Texture texture;
  private TextureRegion textureRegion;
  private RotatableMapComponent rotateComponent;
  private float rotation;

  public DynamicTextureRenderComponent(String texturePath) {
    setTexture(texturePath);
  }

  public DynamicTextureRenderComponent(Texture texture) {
    setTexture(texture);
  }

  @Override
  public void create() {
    super.create();
    rotateComponent = entity.getComponent(RotatableMapComponent.class);
  }

  public void setTexture(String texturePath) {
    texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
    textureRegion = new TextureRegion(texture);
  }

  public void setTexture(Texture texture) {
    this.texture = texture;
    textureRegion = new TextureRegion(texture);
  }

  public void scaleEntity() {
    entity.setScale(1f, (float) texture.getHeight() / texture.getWidth());
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();
    rotation = rotateComponent.getRotation();
    batch.draw(
        textureRegion,
        position.x,
        position.y,
        scale.x / 2f,
        scale.y / 2f,
        scale.x,
        scale.y,
        1f,
        1f,
        rotation);
  }
}
