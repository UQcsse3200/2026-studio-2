package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea.RepeatMode;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;

/** Render multiple layers of a parallax background. */
public class BackgroundRenderComponent extends RenderComponent {

  private final Vector2 backgroundPos;
  private final Vector2 worldBounds;
  private float light;
  private float backgroundLight = 1f;
  private Vector2 lastCameraPos;

  /** A single parallax background layer. */
  private static class ParallaxLayer {
    private final Texture texture;
    private final Vector2 parallaxFactor;
    private float width;
    private float height;
    private final Vector2 offset;
    private final Vector2 velocity;
    private Vector2 position;
    private final RepeatMode repeat;
    private final float distance;
    private float transparency;
    private final boolean flash;

    ParallaxLayer(
        Texture texture,
        Vector2 parallaxFactor,
        float width,
        float height,
        Vector2 offset,
        Vector2 velocity,
        RepeatMode repeat,
        float distance,
        float transparency,
        boolean flash) {

      this.texture = texture;
      this.parallaxFactor = parallaxFactor;
      this.width = width;
      this.height = height;
      this.offset = offset;
      this.velocity = velocity;
      this.position = new Vector2(velocity);
      this.repeat = repeat;
      this.distance = distance;
      this.transparency = transparency;
      this.flash = flash;
    }
  }

  private ArrayList<Vector2> rainPositions = new ArrayList<>();
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
      RepeatMode repeat,
      float distance,
      float transparency,
      boolean flash) {

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
            transparency,
            flash));

    if (repeat == RepeatMode.CHAOTIC) {
      generateRainPositions();
    }
  }

  /** Scale is controlled individually for each layer. */
  public void scaleEntity(Vector2 factor, Vector2 worldBounds, boolean up) {
    // Layer sizes are defined when they are added.
    for (ParallaxLayer layer : layers) {
      if (up) {
        layer.width *= factor.x;
        layer.height *= factor.y;
        layer.offset.x *= factor.x;
        layer.offset.y *= factor.y;
        layer.position.x -= worldBounds.x / 2;
        layer.position.y -= worldBounds.y / 2 + factor.y; // factor is a glue-on fix
        layer.velocity.x *= factor.x;
        layer.velocity.y *= factor.y;
      } else {
        layer.width /= factor.x;
        layer.height /= factor.y;
        layer.offset.x /= factor.x;
        layer.offset.y /= factor.y;
        layer.position.x += worldBounds.x / 2;
        layer.position.y += worldBounds.y / 2 + factor.y; // factor is a glue-on fix
        layer.velocity.x /= factor.x;
        layer.velocity.y /= factor.y;
      }
    }
  }

  /**
   * Calculates incrementing position for layers with non-zero velocity
   *
   * @param layer the layer to get new position for
   */
  private void getPosUpdate(ParallaxLayer layer) {
    // Since this is called every frame, changing frame rates will change speed
    layer.position.x += layer.velocity.x * ServiceLocator.getTimeSource().getDeltaTime();
    layer.position.y += layer.velocity.y * ServiceLocator.getTimeSource().getDeltaTime();
    // Prevent black screen after flash
    if (backgroundLight <= 0.075f) {
      backgroundLight = 0.075f;
    }
    // Keep decrementing light until full night reached
    if (backgroundLight > 0.075f) {
      // backgroundTime -= ServiceLocator.getTimeSource().getDeltaTime() / 1000f;
      backgroundLight = 1f - (ServiceLocator.getTimeSource().getTime() / 30000f); // 50,000
      backgroundLight = 1;
    }
    light = getDarkness();
    // if lightning currently striking
    if (light == 1f) {
      // only flash background if it is dark enough, limit how bright it may flash
      if (backgroundLight < 0.3f) {
        backgroundLight = 0.3f;
      }
    }
    // Flash layers that flash during lightning
    if (layer.flash) {
      if (light == 1f) {
        layer.transparency = 1f;
      } else {
        layer.transparency = 0f;
      }
    }
    // Allow background to get darker than entities
    if (light > backgroundLight) {
      light = backgroundLight;
    }
  }

  private void generateRainPositions() {
    float cameraWidth = camera.getCamera().viewportWidth;
    float cameraHeight = camera.getCamera().viewportHeight;
    float centerX = camera.getCamera().position.x;
    float centerY = camera.getCamera().position.y;
    float startX = centerX - (cameraWidth / 2);
    float endX = centerX + (cameraWidth / 2);
    float startY = centerY + (cameraHeight / 2);
    float endY = centerY - (cameraHeight / 2);
    float currentX;
    float currentY = startY;

    while (currentY > endY) {
      currentX = startX;
      while (currentX < endX) {
        // batch.draw(layer.texture, currentX, currentY, layer.width, layer.height);
        rainPositions.add(new Vector2(currentX, currentY));
        currentX += 0.5f;
      }
      currentY -= 0.5f;
    }

    // distance between horizontal droplets needs to be random
    // distance between vertical droplets needs to be random
    // lowest part of some drops should be lower than highest part of drops on layer beneath it
    // drops should never cross over

    // for entire height
    //    for entire width
    //        from    centerX - (cameraWidth / 2)    TO    centerX + (cameraWidth / 2)

    //    from    centerY - (cameraHeight / 2)    TO    centerY + (cameraHeight / 2)
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
  private Vector2 getPosition(ParallaxLayer layer, Vector2 cameraPos, Vector2 position) {
    float cameraX = cameraPos.x;
    float cameraY = cameraPos.y;
    getPosUpdate(layer);

    float backgroundX =
        position.x + layer.offset.x + cameraX * (1f - layer.parallaxFactor.x) + layer.position.x;
    float backgroundY = position.y + layer.offset.y + cameraY * layer.distance + layer.position.y;

    return new Vector2(backgroundX, backgroundY);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (layers.isEmpty()) {
      return;
    }

    if (camera.getEntity().getComponent(PlayerActions.class) == null) {
      float posX = camera.getCamera().position.x;
      float posY = camera.getCamera().position.y;
      lastCameraPos = new Vector2(posX, posY);
    }

    Vector2 position = entity.getPosition();
    Vector2 cameraPos = lastCameraPos;

    for (ParallaxLayer layer : layers) {
      Vector2 layerPos = null;
      float layerX;
      float layerY;

      layerPos = getPosition(layer, cameraPos, position);

      layerX = layerPos.x;
      layerY = layerPos.y;
      // batch.setColor(0.5f, 0.5f, 0.5f, layer.transparency); Night mode
      Color prevColor = batch.getColor().cpy();
      // time = getDarkness();
      if (layer.flash) {
        batch.setColor(1, 1, 1, layer.transparency);
      } else {
        batch.setColor(light, light, light, layer.transparency);
      }

      batch.draw(layer.texture, layerX, layerY, layer.width, layer.height);
      batch.setColor(prevColor);

      // Draw copies of repeating layers to fill screen
      if (layer.flash) {
        batch.setColor(1, 1, 1, layer.transparency);
      } else {
        batch.setColor(light, light, light, layer.transparency);
      }
      if (layer.repeat == RepeatMode.HORIZONTAL) {
        float newLeftDrawPosX = layerX - layer.width;
        float newRightDrawPosX = layerX + layer.width;

        // if left most x coord of layer >= left most x coord of background pos
        // backgroundPos is used over worldBound.x since backgroundPos extends beyond worldBound
        while (newLeftDrawPosX >= backgroundPos.x - layer.width) {
          // batch.setColor(1f, 1f, 1f, layer.transparency);
          batch.draw(layer.texture, newLeftDrawPosX, layerY, layer.width, layer.height);
          // batch.setColor(Color.WHITE);
          newLeftDrawPosX -= layer.width;
        }

        // if right most x coord of layer <= right of worldBound + extra you can see
        // NOTE: this relies on backgroudPos starting at a negative value, which will always be
        // true if player starts at x = 0
        while (newRightDrawPosX <= worldBounds.x - backgroundPos.x) {
          // batch.setColor(1f, 1f, 1f, layer.transparency);
          batch.draw(layer.texture, newRightDrawPosX, layerY, layer.width, layer.height);
          // batch.setColor(Color.WHITE);
          newRightDrawPosX += layer.width;
        }
      } else if (layer.repeat == RepeatMode.CHAOTIC) {
        /*
        for (Vector2 pos : rainPositions) {
          batch.draw(layer.texture, pos.x, pos.y, layer.width, layer.height);
        }
        */
        float cameraWidth = (float) (int) camera.getCamera().viewportWidth + 4;
        float cameraHeight = (float) (int) camera.getCamera().viewportHeight + 6;
        float centerX = (float) (int) camera.getCamera().position.x;
        float centerY = (float) (int) camera.getCamera().position.y;
        float startX = (float) (int) centerX - (cameraWidth / 2) - 2;
        float endX = (float) (int) centerX + (cameraWidth / 2);
        float startY = (float) (int) centerY + (cameraHeight / 2);
        float endY = (float) (int) centerY - (cameraHeight / 2);
        float currentX;
        float currentY = startY;
        float drawPosX;
        float drawPosY;

        while (currentY > endY) {
          drawPosY = currentY + (layer.position.y % cameraHeight);
          if (drawPosY <= endY) {
            drawPosY += (cameraHeight);
          }
          currentX = startX;
          while (currentX < endX) {
            drawPosX = currentX + (layer.position.x % cameraWidth);
            if (drawPosX >= endX) {
              drawPosX -= (cameraWidth);
            }
            if ((int) (currentY % 2) == 0) {
              batch.draw(layer.texture, drawPosX, drawPosY, layer.width, layer.height);
            } else {
              batch.draw(layer.texture, drawPosX + 0.25f, drawPosY, layer.width, layer.height);
            }

            // rainPositions.add(new Vector2(currentX, currentY));
            currentX += 0.5f;
          }
          currentY -= 0.5f;
        }
      }
      batch.setColor(prevColor);
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
