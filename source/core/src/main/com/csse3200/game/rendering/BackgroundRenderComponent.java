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
   * Adds a parallax layer with a custom size and vertical position.
   *
   * @param texturePath path to the texture
   * @param parallaxFactor controls how much the layer moves relative to player movement
   * @param width width of the layer
   * @param height height of the layer
   * @param offset positional offset relative to backgroundPos
   * @param backgroundType the type of background this layer is
   * @param velocity the independent velocity of the layer
   * @param repeat whether or not this layer should repeat horizontally
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
    layer.position.x += layer.velocity.x / 100;
    layer.position.y += layer.velocity.y / 100;
  }

  /**
   * Get position for layers whose position does not depend on player movement e.g. sky, super
   * distant objects
   *
   * @param layer the layer to calculate position for
   * @param cameraPos the position of the camera
   * @param position the current position of the layer
   * @return updated position of the layer
   */
  private Vector2 getIndependentPosition(ParallaxLayer layer, Vector3 cameraPos, Vector2 position) {
    float cameraX = cameraPos.x;
    float cameraY = cameraPos.y;
    getPosUpdate(layer);

    float backgroundX = cameraX + layer.offset.x;
    float backgroundY = cameraY + layer.offset.y + position.y + layer.position.y;

    return new Vector2(backgroundX, backgroundY);
  }

  /**
   * Get position for layers whose position depends on player movement e.g. mountains, ground,
   * ocean, clouds
   *
   * @param layer the layer to calculate position for
   * @param cameraPos the position of the camera
   * @param position the current position of the layer
   * @return updated position of the layer
   */
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

    for (ParallaxLayer layer : layers) {
      Vector2 layerPos = null;
      float layerX;
      float layerY;

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

      // Draw copies of repeating layers to fill screen
      if (layer.repeat) {
        float newLeftDrawPosX = layerX - layer.width;
        float newRightDrawPosX = layerX + layer.width;

        // if left most x coord of layer >= left most x coord of background pos
        // backgroundPos is used over worldBound.x since backgroundPos extends beyond worldBound
        while (newLeftDrawPosX >= backgroundPos.x - layer.width) {
          batch.draw(layer.texture, newLeftDrawPosX, layerY, layer.width, layer.height);
          newLeftDrawPosX -= layer.width;
        }

        // if right most x coord of layer <= right of worldBound + extra you can see
        // NOTE: this relies on backgroudPos starting at a negative value, which will always be
        // true if player starts at x = 0
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
