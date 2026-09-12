package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.areas.TutorialGameArea.BackgroundType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;

/** Render multiple layers of a parallax background. */
public class BackgroundRenderComponent extends RenderComponent {

  /** A single parallax background layer. */
  private static class ParallaxLayer {
    private final Texture texture;
    private final Vector2 parallaxFactor;
    private final float width;
    private final float height;
    private final float yOffset;
    private final BackgroundType backgroundType;
    private final Vector2 velocity;
    private Vector2 position;

    ParallaxLayer(
        Texture texture,
        Vector2 parallaxFactor,
        float width,
        float height,
        float yOffset,
        BackgroundType backgroundType,
        Vector2 velocity) {

      this.texture = texture;
      this.parallaxFactor = parallaxFactor;
      this.width = width;
      this.height = height;
      this.yOffset = yOffset;
      this.backgroundType = backgroundType;
      this.velocity = velocity;
      this.position = new Vector2(velocity);
    }
  }

  private final List<ParallaxLayer> layers = new ArrayList<>();
  private final CameraComponent camera;

  /**
   * Creates a multi-layer parallax background.
   *
   * @param camera camera used to calculate parallax movement
   */
  public BackgroundRenderComponent(CameraComponent camera) {
    this.camera = camera;
  }

  /**
   * Adds a parallax layer with a custom size and vertical position.
   *
   * @param texturePath internal path of the texture
   * @param parallaxFactor controls how much the layer moves
   * @param width width of the layer in world units
   * @param height height of the layer in world units
   * @param yOffset vertical position relative to the background entity
   */
  public void addLayer(
      String texturePath,
      Vector2 parallaxFactor,
      float width,
      float height,
      float yOffset,
      BackgroundType backgroundType,
      Vector2 velocity) {

    Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);

    layers.add(
        new ParallaxLayer(
            texture, parallaxFactor, width, height, yOffset, backgroundType, velocity));
  }

  /**
   * Adds a parallax layer using its texture dimensions.
   *
   * @param texture texture for the layer
   * @param parallaxFactor controls how much the layer moves
   * @param width width of the layer in world units
   * @param height height of the layer in world units
   * @param yOffset vertical position relative to the background entity
   */
  public void addLayer(
      Texture texture,
      Vector2 parallaxFactor,
      float width,
      float height,
      float yOffset,
      BackgroundType backgroundType,
      Vector2 velocity) {

    layers.add(
        new ParallaxLayer(
            texture, parallaxFactor, width, height, yOffset, backgroundType, velocity));
  }

  /** Scale is controlled individually for each layer. */
  public void scaleEntity() {
    // Layer sizes are defined when they are added.
  }

  private void getPosUpdate(ParallaxLayer layer) {
    layer.position.x += layer.velocity.x / 100;
    layer.position.y += layer.velocity.y / 100;
  }

  private Vector2 getIndependentPosition(ParallaxLayer layer, Vector3 cameraPos, Vector2 position) {
    // For components with constant velocity e.g. sky (velocity = 0)
    // staticVelocity

    // Static is currently static relative to player
    // So will follow player and never change

    float cameraX = cameraPos.x;
    float cameraY = cameraPos.y;
    getPosUpdate(layer);

    float backgroundX = cameraX;
    float backgroundY = position.y + layer.yOffset + cameraY + layer.position.y;

    return new Vector2(backgroundX, backgroundY);
  }

  private Vector2 getDependentPosition(ParallaxLayer layer, Vector3 cameraPos, Vector2 position) {
    // For components dependent on player position e.g. mountains, ground, ocean, clouds, etc.
    // parallaxFactor
    // staticVelocity

    float cameraX = cameraPos.x;
    float cameraY = cameraPos.y;
    getPosUpdate(layer);

    float backgroundX = position.x + cameraX * (1f - layer.parallaxFactor.x) + layer.position.x;
    float backgroundY = position.y + layer.yOffset + cameraY + layer.position.y;

    return new Vector2(backgroundX, backgroundY);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (layers.isEmpty()) {
      return;
    }

    Vector2 position = entity.getPosition();
    Vector3 cameraPos = camera.getCamera().position;

    /*
     * Draw layers from back to front.
     *
     * Lower parallax factors move more slowly.
     * Higher parallax factors move more quickly.
     */
    for (ParallaxLayer layer : layers) {

      Vector2 backgroundPos = null;
      float backgroundX;
      float backgroundY;

      switch (layer.backgroundType) {
        case INDEPENDENT:
          backgroundPos = getIndependentPosition(layer, cameraPos, position);
          break;

        case DEPENDENT:
          backgroundPos = getDependentPosition(layer, cameraPos, position);
          break;
      }

      backgroundX = backgroundPos.x;
      backgroundY = backgroundPos.y;

      batch.draw(layer.texture, backgroundX, backgroundY, layer.width, layer.height);
    }
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
