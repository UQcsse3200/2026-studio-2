package com.csse3200.game.rendering;

import static com.csse3200.game.areas.GameArea.RepeatMode.*;

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
  private boolean weather;
  private Vector2 lastCameraPos;
  private int flashOrder = 0;
  private Vector2 originalVelocity;
  private float originalWidth;
  private float originalHeight;
  private int originalRotation;
  private final Vector2 finalVelocity = new Vector2(5.5f, -4.5f);

  /** A single parallax background layer. */
  private static class ParallaxLayer {
    private Texture texture;
    private final Vector2 parallaxFactor;
    private float width;
    private float height;
    private final Vector2 offset;
    private final Vector2 velocity;
    private final Vector2 position;
    private final RepeatMode repeat;
    private final float distance;
    private float transparency;
    private final boolean flash;
    private int rotation;
    private final int lightningOrder;

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
  private final ArrayList<Vector2> rainPositions = new ArrayList<>();
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
   * @param repeat whether or not this layer should repeat
   * @param distance the distance from POV affecting vertical parallax movement
   * @param transparency the transparency of the layer
   * @param flash whether or not this layer should only be visible during lightning
   * @param rotation how much to rotate the texture of this layer from 0 to 360
   * @param lightningOrder the order this layer will become visible during lightning
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

    if (repeat == RepeatMode.CHAOTIC) {
      originalVelocity = new Vector2(velocity.x, velocity.y);
      originalWidth = width;
      originalHeight = height;
      originalRotation = rotation;
    }
  }

  public int getLayerCount() {
    return layers.size();
  }

  /** Scale is controlled individually for each layer. */
  public void scaleEntity(Vector2 factor, Vector2 worldBounds, boolean up, String level) {
    for (ParallaxLayer layer : layers) {
      if (level.equals("bossArena")) {
        layer.position.x -= 1.7f;
        layer.position.y -= 1.22f;
        layer.height += 1;
        if (layer.flash) {
          layer.height += 3;
        }
      } else {
        if (up) {
          if (level.equals("level2")) {
            layer.position.x -= worldBounds.x / 2 - 10;
            layer.position.y -= factor.y * layer.height / 2.4f;
            layer.width *= factor.x / 1.25f;
            layer.height *= factor.y / 1.25f;
          } else if (level.equals("level3")) {
            layer.position.x -= worldBounds.x / 2 - 5;
            layer.position.y -= worldBounds.y * 0.77f + factor.y;
            layer.width *= factor.x;
            layer.height *= factor.y * 1.17f;
            layer.offset.y *= 1.15f;
          } else {
            layer.position.x -= worldBounds.x / 2 - 10;
            layer.position.y -= worldBounds.y * factor.y / 5.1f;
            layer.width *= factor.x / 1.57f;
            layer.height *= factor.y / 1.4f;
          }
          layer.offset.x *= factor.x;
          layer.offset.y *= factor.y;
          layer.velocity.x *= factor.x;
          layer.velocity.y *= factor.y;
        } else {
          if (level.equals("level2")) {
            layer.position.x += worldBounds.x / 2 - 10;
            // layer.height already multiplied by factor.y, negate 1.25 divided before
            layer.position.y += layer.height / 2.4f * 1.25f;
            layer.width /= factor.x / 1.25f;
            layer.height /= factor.y / 1.25f;
          } else if (level.equals("level3")) {
            layer.position.x += worldBounds.x / 2 - 5;
            layer.position.y += worldBounds.y * 0.77f + factor.y;
            layer.width /= factor.x;
            layer.height /= factor.y * 1.17f;
            layer.offset.y /= 1.15f;
          } else {
            layer.position.x += worldBounds.x / 2 - 10;
            layer.position.y += worldBounds.y * factor.y / 5.1f; // factor is a glue-on fix
            layer.width /= factor.x / 1.57f;
            layer.height /= factor.y / 1.4f;
          }
          layer.offset.x /= factor.x;
          layer.offset.y /= factor.y;
          layer.velocity.x /= factor.x;
          layer.velocity.y /= factor.y;
        }
      }
    }
  }

  /**
   * Calculates incrementing position for layers with non-zero velocity
   *
   * @param layer the layer to get new position for
   */
  private void getPosUpdate(ParallaxLayer layer) {
    layer.position.x += layer.velocity.x * ServiceLocator.getTimeSource().getDeltaTime();
    layer.position.y += layer.velocity.y * ServiceLocator.getTimeSource().getDeltaTime();
    weather = getWeather();
    if (weather) {
      // Flash layers that flash during lightning
      if (layer.flash) {
        float lightning = getLightning();
        // System.out.print(rainIntensity + ", ");

        if (rainIntensity <= 0.3559) {
          if (layer.lightningOrder == 0) {
            layer.texture =
                ServiceLocator.getResourceService()
                    .getAsset("images/parallax/lightning_4.png", Texture.class);
          }
        } else if (rainIntensity <= 0.5678) {
          if (layer.lightningOrder == 1) {
            layer.texture =
                ServiceLocator.getResourceService()
                    .getAsset("images/parallax/lightning_2.png", Texture.class);
          }
        } else if (rainIntensity <= 0.8205) {
          if (layer.lightningOrder == 0) {
            layer.texture =
                ServiceLocator.getResourceService()
                    .getAsset("images/parallax/lightning_3.png", Texture.class);
          }
        }

        // Get lightning order
        if (lightning > 5f && lightning < 5.8f) {
          flashOrder = 0;
        } else if (lightning > 13f && lightning < 13.3f) {
          flashOrder = 1;
        }
        // Flash single lightning layer
        if (layer.lightningOrder == flashOrder) {
          if (darkness == 1.1f) {
            layer.transparency = 1f;
          } else {
            layer.transparency = 0f;
          }
        }
      }
    }
  }

  private void updateWeather(ParallaxLayer layer) {
    weather = getWeather();
    if (weather) {
      if (layer.repeat == CHAOTIC) {
        if (rainIntensity < 1f) {
          float intensity = 1f - rainIntensity;
          float finalWidth = 0.1f;
          float finalHeight = 0.45f;
          int finalRotation = 40;
          layer.velocity.x =
              originalVelocity.x + (finalVelocity.x - originalVelocity.x) * intensity;
          layer.velocity.y =
              originalVelocity.y + (finalVelocity.y - originalVelocity.y) * intensity;
          layer.width = originalWidth + (finalWidth - originalWidth) * intensity;
          layer.height = originalHeight + (finalHeight - originalHeight) * intensity;
          layer.rotation =
              (int) (originalRotation + (finalRotation - originalRotation) * intensity);
        }
      }
      if (layer.flash) {}
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
      updateWeather(layer);

      layerX = layerPos.x;
      layerY = layerPos.y;
      Color prevColor = batch.getColor().cpy();
      if (layer.flash) {
        batch.setColor(
            1, 1, 1, layer.transparency); // setting 0,0,0 gives black lightning looks sick
      } else {
        batch.setColor(backgroundLight, backgroundLight, backgroundLight, layer.transparency);
      }

      batch.draw(layer.texture, layerX, layerY, layer.width, layer.height);
      batch.setColor(prevColor);

      // Draw copies of repeating layers to fill screen
      if (layer.flash) {
        batch.setColor(1, 1, 1, layer.transparency);
      } else {
        batch.setColor(backgroundLight, backgroundLight, backgroundLight, layer.transparency);
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
        // gap between raindrops, works best if gap * int = 1, where int is any positive integer
        float gap = 0.5f;
        // random vertical offset applied to each rain drop in range -verticalOffset to
        // verticalOffset
        float verticalOffset = 0.075f;
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

        // Compute array of offsets, so each raindrop has a set offset
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

        batch.setColor(0.65f, 0.65f, 0.65f, layer.transparency);
        // batch.setColor(1f, 1f, 1f, layer.transparency); //maybe this one
        // batch.setColor(0.65f, 0.65f, 0.65f, 1f);
        // batch.setColor(1f, 1f, 1f, 1f); NOT THIS ONE

        // batch.setColor(0.4f, 0.4f, 1f, 0.5f); //GREAT
        // batch.setColor(0.5f, 0.5f, 1f, 0.5f); //GREAT
        // batch.setColor(0.4f, 0.4f, 1f, 0.4f); //doesnt flash white enough
        // batch.setColor(0.5f, 0.5f, 1f, 0.4f); //mmmm idk
        // batch.setColor(0.6f, 0.6f, 1f, 0.5f); //BEST SO FAR
        // batch.setColor(0.55f, 0.55f, 1f, 0.5f); //ALSO VERY GOOD
        batch.setColor(0.575f, 0.575f, 1f, 0.5f); // 0.5, 0.6, 0.4

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
            // if drawPos is off-screen, loop it back to left
            if (drawPosX >= endX) {
              drawPosX -= (cameraWidth);
            }

            // add a horizontal offset to every 2nd layer
            // add random vertical offset to every raindrop
            if ((int) ((currentY * (1 / gap)) % 2) == 0) {
              batch.draw(
                  layer.texture,
                  drawPosX + rainOffsets.get(randomOffsetY).get(randomOffsetX),
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
                  drawPosX + (gap / 2) + rainOffsets.get(randomOffsetY).get(randomOffsetX),
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
