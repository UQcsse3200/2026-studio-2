package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.areas.GameArea.BackgroundType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;

/** Render multiple layers of a parallax background. */
public class BackgroundRenderComponent extends RenderComponent {

  Vector2 backgroundPos;
  Vector2 worldBounds;

  /** A single parallax background layer. */
  private static class ParallaxLayer {
    private final Texture texture;
    private final Vector2 parallaxFactor;
    private final float width;
    private final float height;
    private final Vector2 offset;
    private final BackgroundType backgroundType;
    private final Vector2 velocity;
    private Vector2 position;
    private final boolean repeat;

    ParallaxLayer(
        Texture texture,
        Vector2 parallaxFactor,
        float width,
        float height,
        Vector2 offset,
        BackgroundType backgroundType,
        Vector2 velocity,
        boolean repeat) {

      this.texture = texture;
      this.parallaxFactor = parallaxFactor;
      this.width = width;
      this.height = height;
      this.offset = offset;
      this.backgroundType = backgroundType;
      this.velocity = velocity;
      this.position = new Vector2(velocity);
      this.repeat = repeat;
    }
  }

  private final List<ParallaxLayer> layers = new ArrayList<>();
  private final CameraComponent camera;

  /**
   * Creates a multi-layer parallax background.
   *
   * @param camera camera used to calculate parallax movement
   */
  public BackgroundRenderComponent(
      CameraComponent camera, Vector2 backgroundPos, Vector2 worldBounds) {
    this.camera = camera;
    this.backgroundPos = backgroundPos;
    this.worldBounds = worldBounds;
  }

  /**
   * Adds a parallax layer with a custom size and vertical position.
   *
   * @param texturePath internal path of the texture
   * @param parallaxFactor controls how much the layer moves
   * @param width width of the layer in world units
   * @param height height of the layer in world units
   * @param offset position relative to the background entity
   */
  public void addLayer(
      String texturePath,
      Vector2 parallaxFactor,
      float width,
      float height,
      Vector2 offset,
      BackgroundType backgroundType,
      Vector2 velocity,
      boolean repeat) {

    Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);

    layers.add(
        new ParallaxLayer(
            texture, parallaxFactor, width, height, offset, backgroundType, velocity, repeat));
  }

  /**
   * Adds a parallax layer using its texture dimensions.
   *
   * @param texture texture for the layer
   * @param parallaxFactor controls how much the layer moves
   * @param width width of the layer in world units
   * @param height height of the layer in world units
   * @param offset position relative to the background entity
   */
  public void addLayer(
      Texture texture,
      Vector2 parallaxFactor,
      float width,
      float height,
      Vector2 offset,
      BackgroundType backgroundType,
      Vector2 velocity,
      boolean repeat) {

    // Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);

    layers.add(
        new ParallaxLayer(
            texture, parallaxFactor, width, height, offset, backgroundType, velocity, repeat));
  }

  /** Scale is controlled individually for each layer. */
  public void scaleEntity() {
    // Layer sizes are defined when they are added.
  }

  private void getPosUpdate(ParallaxLayer layer) {
    // Since this is called every frame, changing frame rates will change speed
    layer.position.x += layer.velocity.x / 100;
    layer.position.y += layer.velocity.y / 100;
  }

  private Vector2 getIndependentPosition(ParallaxLayer layer, Vector3 cameraPos, Vector2 position) {
    float cameraX = cameraPos.x;
    float cameraY = cameraPos.y;
    getPosUpdate(layer);

    float backgroundX = cameraX + layer.offset.x;
    float backgroundY = cameraY + layer.offset.y + position.y + layer.position.y;

    return new Vector2(backgroundX, backgroundY);
  }

  private Vector2 getDependentPosition(ParallaxLayer layer, Vector3 cameraPos, Vector2 position) {
    float cameraX = cameraPos.x;
    float cameraY = cameraPos.y;
    getPosUpdate(layer);

    float backgroundX =
        position.x + layer.offset.x + cameraX * (1f - layer.parallaxFactor.x) + layer.position.x;
    float backgroundY = position.y + layer.offset.y + cameraY + layer.position.y;

    return new Vector2(backgroundX, backgroundY);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (layers.isEmpty()) {
      return;
    }

    Vector2 position = entity.getPosition();
    Vector3 cameraPos = camera.getCamera().position;
    Vector2 layerPos = null;
    float layerX;
    float layerY;

    /*
     * Draw layers from back to front.
     *
     * Lower parallax factors move more slowly.
     * Higher parallax factors move more quickly.
     */
    for (ParallaxLayer layer : layers) {

      switch (layer.backgroundType) {
        case INDEPENDENT:
          layerPos = getIndependentPosition(layer, cameraPos, position);
          break;

        case DEPENDENT:
          layerPos = getDependentPosition(layer, cameraPos, position);
          break;
      }

      layerX = layerPos.x;
      layerY = layerPos.y;

      batch.draw(layer.texture, layerX, layerY, layer.width, layer.height);

      if (layer.repeat) {
        float newLeftDrawPosX = layerX - layer.width;
        float newRightDrawPosX = layerX + layer.width;

        while (newLeftDrawPosX >= backgroundPos.x - layer.width) {
          batch.draw(layer.texture, newLeftDrawPosX, layerY, layer.width, layer.height);
          newLeftDrawPosX -= layer.width;
        }
        while (newRightDrawPosX <= worldBounds.x - backgroundPos.x) {
          batch.draw(layer.texture, newRightDrawPosX, layerY, layer.width, layer.height);
          newRightDrawPosX += layer.width;
        }
      }
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
