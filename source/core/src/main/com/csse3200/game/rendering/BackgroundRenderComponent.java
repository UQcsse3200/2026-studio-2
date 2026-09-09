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

  private Vector2 staticPosUpdate = new Vector2(0, 0);

  /** A single parallax background layer. */
  private static class ParallaxLayer {
    private final Texture texture;
    private final Vector2 parallaxFactor;
    private final Vector2 staticVelocity;
    private final float width;
    private final float height;
    private final float yOffset;
    private final BackgroundType backgroundType;

    ParallaxLayer(
        Texture texture,
        Vector2 parallaxFactor,
        Vector2 staticVelocity,
        float width,
        float height,
        float yOffset,
        BackgroundType backgroundType) {

      this.texture = texture;
      this.parallaxFactor = parallaxFactor;
      this.staticVelocity = staticVelocity;
      this.width = width;
      this.height = height;
      this.yOffset = yOffset;
      this.backgroundType = backgroundType;
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
      Vector2 staticVelocity,
      float width,
      float height,
      float yOffset,
      BackgroundType backgroundType) {

    Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);

    layers.add(
        new ParallaxLayer(
            texture, parallaxFactor, staticVelocity, width, height, yOffset, backgroundType));
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
      Vector2 staticVelocity,
      float width,
      float height,
      float yOffset,
      BackgroundType backgroundType) {

    layers.add(
        new ParallaxLayer(
            texture, parallaxFactor, staticVelocity, width, height, yOffset, backgroundType));
  }

  /** Scale is controlled individually for each layer. */
  public void scaleEntity() {
    // Layer sizes are defined when they are added.
  }

  private Vector2 getPositionUpdate(Vector2 staticVelocity) {
    staticPosUpdate.x += staticVelocity.x;
    staticPosUpdate.y += staticVelocity.y;

    return new Vector2(
        staticVelocity.x + staticPosUpdate.x / 100, staticVelocity.y + staticPosUpdate.y / 100);
  }

  private Vector2 getStaticPosition(
      ParallaxLayer layer, Vector3 cameraPos, Vector2 position, Vector2 staticVelocity) {
    // For components with constant velocity e.g. sky (velocity = 0), clouds (velocity = 1)
    // staticVelocity

    float cameraX = cameraPos.x;
    float cameraY = cameraPos.y;
    Vector2 posUpdate = getPositionUpdate(staticVelocity);

    float backgroundX = position.x + cameraX + posUpdate.x;
    float backgroundY = position.y + layer.yOffset + cameraY + posUpdate.y;

    return new Vector2(backgroundX, backgroundY);
  }

  private Vector2 getDependentPosition(ParallaxLayer layer, Vector3 cameraPos, Vector2 position) {
    // For components dependent on player position e.g. mountains, ground etc.
    // parallaxFactor
    // staticVelocity

    float cameraX = cameraPos.x;
    float cameraY = cameraPos.y;

    float backgroundX = position.x + cameraX * (1f - layer.parallaxFactor.x);
    float backgroundY = position.y + layer.yOffset + cameraY;

    return new Vector2(backgroundX, backgroundY);
  }

  private Vector2 getCombinedPosition(Vector2 parallaxFactor, Vector2 staticVelocity) {
    // For components dependent on player pos + constant velocity e.g. ocean
    return new Vector2(0, 0);
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
        case STATIC:
          backgroundPos = getStaticPosition(layer, cameraPos, position, layer.staticVelocity);
          break;

        case DEPENDENT:
          backgroundPos = getDependentPosition(layer, cameraPos, position);
          break;

        case COMBINED:
          // backgroundPos = getCombinedPosition(layer, cameraX, cameraY, position);
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
