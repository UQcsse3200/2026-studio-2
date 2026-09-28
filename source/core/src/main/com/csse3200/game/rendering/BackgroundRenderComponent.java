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
import java.util.Random;

/** Render multiple layers of a parallax background. */
public class BackgroundRenderComponent extends RenderComponent {

  private final Vector2 backgroundPos;
  private final Vector2 worldBounds;
  private float light;
  private float backgroundLight = 1f;
  private Vector2 lastCameraPos;
  private int flashOrder = 0;

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
    private int rotation;
    private int lightningOrder;

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
        boolean flash,
        int rotation,
        int lightningOrder) {

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
      this.rotation = rotation;
      this.lightningOrder = lightningOrder;
    }
  }

  private ArrayList<ArrayList<Float>> rainOffsets = null;
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
      boolean flash,
      int rotation,
      int lightningOrder) {

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
            flash,
            rotation,
            lightningOrder));
    // if (repeat == RepeatMode.CHAOTIC) {
    //  generateRainPositions();
    // }
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
    if (backgroundLight <= 0.125f) {
      backgroundLight = 0.125f;
    }
    // Keep decrementing light until full night reached
    if (backgroundLight > 0.125f) {
      // backgroundTime -= ServiceLocator.getTimeSource().getDeltaTime() / 1000f;
      backgroundLight = 1f - (ServiceLocator.getTimeSource().getTime() / 40000f); // 50,000
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
      float lightning = getLightning();
      // Get lightning order
      if (lightning > 5f && lightning < 5.8f) {
        flashOrder = 0;
      } else if (lightning > 13f && lightning < 13.3f) {
        flashOrder = 1;
      }
      // Flash single lightning layer
      if (layer.lightningOrder == flashOrder) {
        if (light == 1f) {
          layer.transparency = 1f;
        } else {
          layer.transparency = 0f;
        }
      }
    }
    // Allow background to get darker than entities
    if (light > backgroundLight) {
      light = backgroundLight;
    }
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

        /// TODO
        /// make gap a customiseable variable on layer instantiation
        /// make rotation a variable of addLayer
        /// make system that can transition to heavier storms
        /// add lightning/rain sounds for light/medium/heavy

        /// offset every 2nd vertical layer by (gap / 2)
        /// add tiny random +/- y adjustments to each raindrop

        // so with heavier rain i will use longer rain
        // will need to custom make its size, gap, speed, angle

        // gap between raindrops, works best if gap * int = 1, where int is any positive integer
        float gap = 0.5f;
        float verticalOffset = 0.05f;
        float cameraWidth = (float) (int) camera.getCamera().viewportWidth + 4;
        float cameraHeight = (float) (int) camera.getCamera().viewportHeight + 6;
        float centerX = (float) (int) camera.getCamera().position.x;
        float centerY = (float) (int) camera.getCamera().position.y;
        float startX = (float) (int) centerX - (cameraWidth / 2);
        float endX = (float) (int) centerX + (cameraWidth / 2);
        float startY = (float) (int) centerY + (cameraHeight / 2);
        float endY = (float) (int) centerY - (cameraHeight / 2);
        float currentX;
        float currentY = startY;
        float drawPosX;
        float drawPosY;

        if (rainOffsets == null) {
          Random random = new Random();
          rainOffsets = new ArrayList<>();
          int y = 0;
          while (currentY > endY) {
            currentX = startX;
            rainOffsets.add(new ArrayList<>());
            while (currentX < endX) {
              float microOffset = random.nextFloat();
              float micro = -verticalOffset + microOffset * 2 * verticalOffset;
              rainOffsets.get(y).add(micro);
              currentX += gap;
            }
            currentY -= gap;
            y++;
          }
        }

        int lengthY = rainOffsets.size();
        int lengthX = rainOffsets.getFirst().size();

        int randomOffsetX;
        int randomOffsetY = 0;
        currentY = startY;
        while (currentY > endY) {
          // draw particle at currentY, ensuring it loops indefinitely
          drawPosY = currentY + (layer.position.y % cameraHeight);
          // if drawPos is off-screen, loop it back to top
          if (drawPosY <= endY) {
            drawPosY += (cameraHeight);
          }
          currentX = startX;
          randomOffsetX = 0;
          while (currentX < endX) {
            // draw particle at currentX, ensuring it loops indefinitely
            drawPosX = currentX + (layer.position.x % cameraWidth);
            // if drawPos is off-screen, loop it back to top
            if (drawPosX >= endX) {
              drawPosX -= (cameraWidth);
            }

            // add a horizontal offset to every 2nd layer
            if ((int) ((currentY * (1 / gap)) % 2) == 0) {
              batch.draw(
                  layer.texture,
                  drawPosX,
                  drawPosY + rainOffsets.get(randomOffsetY).get(randomOffsetX),
                  layer.width / 2,
                  layer.height / 2,
                  layer.width,
                  layer.height,
                  1f,
                  1f,
                  layer.rotation,
                  0,
                  0,
                  layer.texture.getWidth(),
                  layer.texture.getHeight(),
                  false,
                  false);
            } else {
              batch.draw(
                  layer.texture,
                  drawPosX + (gap / 2),
                  drawPosY + rainOffsets.get(randomOffsetY).get(randomOffsetX),
                  layer.width / 2,
                  layer.height / 2,
                  layer.width,
                  layer.height,
                  1f,
                  1f,
                  layer.rotation,
                  0,
                  0,
                  layer.texture.getWidth(),
                  layer.texture.getHeight(),
                  false,
                  false);
            }
            currentX += gap;
            // Safety check for when map is activated
            if (randomOffsetX < lengthX - 1) {
              randomOffsetX++;
            }
          }
          currentY -= gap;
          // Safety check for when map is activated
          if (randomOffsetY < lengthY - 1) {
            randomOffsetY++;
          }
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
