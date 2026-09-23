package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
    private final Vector2 velocity;
    private Vector2 startPos;
    private final boolean repeat;
    private final float distance;
    private final float transparency;

    ParallaxLayer(
        Texture texture,
        Vector2 parallaxFactor,
        float width,
        float height,
        Vector2 offset,
        Vector2 velocity,
        boolean repeat,
        float distance,
        float transparency) {

      this.texture = texture;
      this.parallaxFactor = parallaxFactor;
      this.width = width;
      this.height = height;
      this.offset = offset;
      this.velocity = velocity;
      this.startPos = new Vector2(velocity);
      this.repeat = repeat;
      this.distance = distance;
      this.transparency = transparency;
    }
  }

  private final List<ParallaxLayer> layers = new ArrayList<>();
  private final CameraComponent camera;

  /**
   * Creates a multi-layer parallax background.
   *
   * @param camera camera used to calculate parallax movement
   * @param backgroundPos the position of the background
   * @param worldBounds the maximum x and y values of the world bounds
   */
  public BackgroundRenderComponent(
      CameraComponent camera, Vector2 backgroundPos, Vector2 worldBounds) {
    this.camera = camera;
    this.backgroundPos = backgroundPos;
    this.worldBounds = worldBounds;
  }

  /**
   * Adds a parallax layer with a custom size and vertical position. Distance is a float between 0
   * and 1. 1 causes the background to have no vertical movement, while 0 causes it to follow player
   * directly.
   *
   * @param texturePath path to the texture
   * @param parallaxFactor controls how much the layer moves relative to player movement
   * @param width width of the layer
   * @param height height of the layer
   * @param offset positional offset relative to backgroundPos
   * @param velocity the independent velocity of the layer
   * @param repeat whether or not this layer should repeat horizontally
   * @param distance the distance from POV affecting vertical parallax movement
   * @param transparency the transparency of the layer
   */
  public void addLayer(
      String texturePath,
      Vector2 parallaxFactor,
      float width,
      float height,
      Vector2 offset,
      Vector2 velocity,
      boolean repeat,
      float distance,
      float transparency) {

    Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);

    layers.add(
        new ParallaxLayer(
            texture,
            parallaxFactor,
            width,
            height,
            offset,
            velocity,
            repeat,
            distance,
            transparency));
  }

  /** Scale is controlled individually for each layer. */
  public void scaleEntity() {
    // Layer sizes are defined when they are added.
  }

  /**
   * Calculates incrementing position for layers with non-zero velocity
   *
   * @param layer the layer to get new position for
   */
  private void getPosUpdate(ParallaxLayer layer) {
    // Since this is called every frame, changing frame rates will change speed
    layer.startPos.x += layer.velocity.x / 100;
    layer.startPos.y += layer.velocity.y / 100;
  }

  /**
   * Get position for layers whose position depends on player movement e.g. mountains, ground,
   * ocean, clouds
   *
   * @param layer the layer to calculate position for
   * @param cameraPos the position of the camera
   * @param currentPos the current position of the layer
   * @return updated position of the layer
   */
  private Vector2 getPosition(ParallaxLayer layer, Vector3 cameraPos, Vector2 currentPos) {
    float cameraX = cameraPos.x;
    float cameraY = cameraPos.y;
    getPosUpdate(layer);

    float backgroundX =
        currentPos.x + layer.offset.x + cameraX * (1f - layer.parallaxFactor.x) + layer.startPos.x;
    float backgroundY = currentPos.y + layer.offset.y + cameraY * layer.distance + layer.startPos.y;

    return new Vector2(backgroundX, backgroundY);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (layers.isEmpty()) {
      return;
    }

    Vector2 currentPos = entity.getPosition();
    Vector3 cameraPos = camera.getCamera().position;

    for (ParallaxLayer layer : layers) {
      Vector2 layerPos = null;
      float layerX;
      float layerY;

      layerPos = getPosition(layer, cameraPos, currentPos);

      layerX = layerPos.x;
      layerY = layerPos.y;
      batch.setColor(1f, 1f, 1f, layer.transparency);
      batch.draw(layer.texture, layerX, layerY, layer.width, layer.height);
      batch.setColor(Color.WHITE);

      // Draw copies of repeating layers to fill screen
      if (layer.repeat) {
        float newLeftDrawPosX = layerX - layer.width;
        float newRightDrawPosX = layerX + layer.width;

        // if left most x coord of layer >= left most x coord of background pos
        // backgroundPos is used over worldBound.x since backgroundPos extends beyond worldBound
        while (newLeftDrawPosX >= backgroundPos.x - layer.width) {
          batch.setColor(1f, 1f, 1f, layer.transparency);
          batch.draw(layer.texture, newLeftDrawPosX, layerY, layer.width, layer.height);
          batch.setColor(Color.WHITE);
          newLeftDrawPosX -= layer.width;
        }

        // if right most x coord of layer <= right of worldBound + extra you can see
        // NOTE: this relies on backgroudPos starting at a negative value, which will always be
        // true if player starts at x = 0
        while (newRightDrawPosX <= worldBounds.x - backgroundPos.x) {
          batch.setColor(1f, 1f, 1f, layer.transparency);
          batch.draw(layer.texture, newRightDrawPosX, layerY, layer.width, layer.height);
          batch.setColor(Color.WHITE);
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
