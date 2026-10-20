package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.level.RotatableMapComponent;
import com.csse3200.game.services.ServiceLocator;

public class DynamicTextureRenderComponent extends RenderComponent implements CullingRenderable {
  private Texture texture;
  private TextureRegion textureRegion;
  private RotatableMapComponent rotateComponent;
  private boolean keepAspectRatio;

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

  public Texture getTexture() {
    return texture;
  }

  public void setTexture(String texturePath) {
    texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
    textureRegion = new TextureRegion(texture);
  }

  /**
   * Draws only a rectangular slice of the texture, e.g. one tile's worth of a wide strip.
   *
   * @param x left edge of the slice in texture pixels
   * @param y top edge of the slice in texture pixels
   * @param width width of the slice in pixels
   * @param height height of the slice in pixels
   * @return self
   */
  public DynamicTextureRenderComponent setRegion(int x, int y, int width, int height) {
    textureRegion = new TextureRegion(texture, x, y, width, height);
    return this;
  }

  public void setTexture(Texture texture) {
    this.texture = texture;
    textureRegion = new TextureRegion(texture);
  }

  /**
   * Draws the texture with its own proportions, centred inside the entity's scale, instead of
   * stretching it to fill the entity. The entity's size, collider and centre stay unchanged.
   *
   * @return self
   */
  public DynamicTextureRenderComponent keepAspectRatio() {
    this.keepAspectRatio = true;
    return this;
  }

  public void scaleEntity() {
    entity.setScale(1f, (float) texture.getHeight() / texture.getWidth());
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();
    float width = scale.x;
    float height = scale.y;
    if (keepAspectRatio) {
      float textureAspect =
          (float) textureRegion.getRegionWidth() / textureRegion.getRegionHeight();
      width = Math.min(scale.x, scale.y * textureAspect);
      height = width / textureAspect;
    }
    float drawX = position.x + (scale.x - width) / 2f;
    float drawY = position.y + (scale.y - height) / 2f;
    float rotation = rotateComponent != null ? rotateComponent.getRotation() : 0f;
    Color prevColor = batch.getColor().cpy();

    batch.draw(
        textureRegion, drawX, drawY, width / 2f, height / 2f, width, height, 1f, 1f, rotation);
    batch.setColor(prevColor);
  }

  @Override
  public boolean isVisible(Camera camera) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();
    float rotation = rotateComponent != null ? rotateComponent.getRotation() : 0f;
    return RenderCulling.isVisible(
        camera.frustum, position.x, position.y, scale.x, scale.y, rotation);
  }
}
