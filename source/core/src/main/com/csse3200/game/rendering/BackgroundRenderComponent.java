package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.services.ServiceLocator;

/** Render a parallax background texture. */
public class BackgroundRenderComponent extends RenderComponent {

  private final Texture texture;
  private final CameraComponent camera;
  private final float parallaxFactor;

  /**
   * Creates a parallax background from a texture path.
   *
   * @param texturePath internal path of the background texture
   * @param camera camera used to calculate parallax movement
   * @param parallaxFactor controls the background movement speed
   */
  public BackgroundRenderComponent(
      String texturePath, CameraComponent camera, float parallaxFactor) {

    this(
        ServiceLocator.getResourceService().getAsset(texturePath, Texture.class),
        camera,
        parallaxFactor);
  }

  /**
   * Creates a parallax background from a texture.
   *
   * @param texture background texture
   * @param camera camera used to calculate parallax movement
   * @param parallaxFactor controls the background movement speed
   */
  public BackgroundRenderComponent(Texture texture, CameraComponent camera, float parallaxFactor) {

    this.texture = texture;
    this.camera = camera;
    this.parallaxFactor = parallaxFactor;
  }

  /** Scale the entity to a width of 1 and matching texture ratio. */
  public void scaleEntity() {
    entity.setScale(1f, (float) texture.getHeight() / texture.getWidth());
  }

  @Override
  protected void draw(SpriteBatch batch) {

    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();

    // Get the camera's current horizontal position.
    float cameraX = camera.getCamera().position.x;

    // Move the background more slowly than the camera.
    float backgroundX = position.x + cameraX * (1f - parallaxFactor);

    // Keep the background vertically fixed.
    float backgroundY = position.y;

    batch.draw(texture, backgroundX, backgroundY, scale.x, scale.y);
  }

  @Override
  public int getLayer() {
    return 0;
  }

  @Override
  public float getZIndex() {
    return -1f;
  }
}
