package com.csse3200.game.rendering;

import static com.csse3200.game.areas.GameArea.RepeatMode.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
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
  private Vector2 lastCameraPos;
  private int flashOrder = 0;
  private Vector2 originalVelocity;
  private float originalWidth;
  private float originalHeight;
  private int originalRotation;
  private final Vector2 finalVelocity = new Vector2(5.5f, -4.5f);

  private float SCALE_TIME = 0.75f;
  private float currentScaleTime = 0f;
  private boolean scalingBackground = false;

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

    private float startWidth;
    private float startHeight;
    private float targetWidth;
    private float targetHeight;
    private final Vector2 startOffset = new Vector2();
    private final Vector2 targetOffset = new Vector2();
    private final Vector2 startVelocity = new Vector2();
    private final Vector2 targetVelocity = new Vector2();
    private final Vector2 startPosition = new Vector2();
    private final Vector2 targetPosition = new Vector2();
    private float baseWidth;
    private float baseHeight;
    private final Vector2 baseOffset = new Vector2();
    private final Vector2 baseVelocity = new Vector2();
    private final Vector2 basePosition = new Vector2();

    ParallaxLayer(Texture texture, LayerLayout layout, LayerAppearance appearance) {
      this.texture = texture;
      this.parallaxFactor = layout.parallaxFactor();
      this.width = layout.width();
      this.height = layout.height();
      this.offset = layout.offset();
      this.velocity = layout.velocity();
      this.position = new Vector2(layout.velocity());
      this.repeat = layout.repeat();
      this.distance = layout.distance();
      this.transparency = appearance.transparency();
      this.flash = appearance.flash();
      this.rotation = appearance.rotation();
      this.lightningOrder = appearance.lightningOrder();
    }
  }

  /** Geometry and movement of a parallax layer. */
  public record LayerLayout(
      Vector2 parallaxFactor,
      float width,
      float height,
      Vector2 offset,
      Vector2 velocity,
      RepeatMode repeat,
      float distance) {}

  /** Visual and lightning settings for a parallax layer. */
  public record LayerAppearance(
      float transparency, boolean flash, int rotation, int lightningOrder) {}

  private ArrayList<ArrayList<Float>> rainOffsets = null;
  // Created once and reused for the rain drop offsets.
  private final Random random = new Random();
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

  /** Adds a layer with its geometry, movement, and visual settings. */
  public void addLayer(String texturePath, LayerLayout layout, LayerAppearance appearance) {
    Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
    layers.add(new ParallaxLayer(texture, layout, appearance));
    if (layout.repeat() == RepeatMode.CHAOTIC) {
      originalVelocity = new Vector2(layout.velocity());
      originalWidth = layout.width();
      originalHeight = layout.height();
      originalRotation = appearance.rotation();
    }
  }

  public int getLayerCount() {
    return layers.size();
  }

  private void scaleUp(ParallaxLayer layer, Vector2 factor, Vector2 worldBounds, String level) {
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
  }

  private void scaleDown(ParallaxLayer layer, Vector2 factor, Vector2 worldBounds, String level) {
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

  /** Scale is controlled individually for each layer. */
  public void scaleEntity(Vector2 factor, Vector2 worldBounds, boolean up, String level) {
    if (level.equals("bossArena")) {
      for (ParallaxLayer layer : layers) {
        if (layer.baseWidth > 0f) {
          layer.width = layer.baseWidth;
          layer.height = layer.baseHeight;
          layer.offset.set(layer.baseOffset);
          layer.velocity.set(layer.baseVelocity);
          layer.position.set(layer.basePosition);
        }

        layer.position.x -= 1.7f;
        layer.position.y -= 1.22f;
        layer.height += 1;
        if (layer.flash) {
          layer.height += 3;
        }
      }
      scalingBackground = false;
      currentScaleTime = 0f;
      return;
    }

    currentScaleTime = 0f;
    scalingBackground = true;

    for (ParallaxLayer layer : layers) {
      layer.startWidth = layer.width;
      layer.startHeight = layer.height;
      layer.startOffset.set(layer.offset);
      layer.startVelocity.set(layer.velocity);
      layer.startPosition.set(layer.position);

      if (up) {
        if (up) {
          layer.baseWidth = layer.width;
          layer.baseHeight = layer.height;
          layer.baseOffset.set(layer.offset);
          layer.baseVelocity.set(layer.velocity);
          layer.basePosition.set(layer.position);
        }

        scaleUp(layer, factor, worldBounds, level);
      } else {
        scaleDown(layer, factor, worldBounds, level);
      }

      // now that the targets have been calculated, store them and reset so we can properly animate
      layer.targetWidth = layer.width;
      layer.targetHeight = layer.height;
      layer.targetOffset.set(layer.offset);
      layer.targetVelocity.set(layer.velocity);
      layer.targetPosition.set(layer.position);

      // reset all state changes to animate them in update
      layer.width = layer.startWidth;
      layer.height = layer.startHeight;
      layer.offset.set(layer.startOffset);
      layer.velocity.set(layer.startVelocity);
      layer.position.set(layer.startPosition);
    }
  }

  /**
   * Switches lightning layer textures.
   *
   * @param layer the layer to switch textures for
   */
  private void switchLightningLayer(ParallaxLayer layer) {
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
    } else if (rainIntensity <= 0.8205 && layer.lightningOrder == 0) {
      layer.texture =
          ServiceLocator.getResourceService()
              .getAsset("images/parallax/lightning_3.png", Texture.class);
    }
  }

  /**
   * Calculates incrementing position for layers with non-zero velocity
   *
   * @param layer the layer to get new position for
   */
  private void getPosUpdate(ParallaxLayer layer) {
    if (!scalingBackground) {
      layer.position.x += layer.velocity.x * ServiceLocator.getTimeSource().getDeltaTime();
      layer.position.y += layer.velocity.y * ServiceLocator.getTimeSource().getDeltaTime();
    }

    boolean weather = getWeather();
    if (weather && layer.flash) {
      // Flash layers that flash during lightning
      float lightning = getLightning();
      switchLightningLayer(layer);

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

  /**
   * Updates rain layers speed, height and rotation for different rain intensities
   *
   * @param layer the layer to adjust the weather for
   */
  private void updateWeather(ParallaxLayer layer) {
    boolean weather = getWeather();
    if (weather && layer.repeat == CHAOTIC && rainIntensity < 1f) {
      float intensity = 1f - rainIntensity;
      float finalWidth = 0.15f;
      float finalHeight = 0.45f;
      int finalRotation = 40;
      layer.velocity.x = originalVelocity.x + (finalVelocity.x - originalVelocity.x) * intensity;
      layer.velocity.y = originalVelocity.y + (finalVelocity.y - originalVelocity.y) * intensity;
      layer.width = originalWidth + (finalWidth - originalWidth) * intensity;
      layer.height = originalHeight + (finalHeight - originalHeight) * intensity;
      layer.rotation = (int) (originalRotation + (finalRotation - originalRotation) * intensity);
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
  public void update() {
    super.update();
    if (scalingBackground) {
      currentScaleTime += ServiceLocator.getTimeSource().getDeltaTime();
      float progress = Math.min(1f, currentScaleTime / SCALE_TIME);

      for (ParallaxLayer layer : layers) {
        // lerp all values
        layer.width = MathUtils.lerp(layer.startWidth, layer.targetWidth, progress);
        layer.height = MathUtils.lerp(layer.startHeight, layer.targetHeight, progress);
        layer.offset.x = MathUtils.lerp(layer.startOffset.x, layer.targetOffset.x, progress);
        layer.offset.y = MathUtils.lerp(layer.startOffset.y, layer.targetOffset.y, progress);
        layer.velocity.x = MathUtils.lerp(layer.startVelocity.x, layer.targetVelocity.x, progress);
        layer.velocity.y = MathUtils.lerp(layer.startVelocity.y, layer.targetVelocity.y, progress);
        layer.position.x = MathUtils.lerp(layer.startPosition.x, layer.targetPosition.x, progress);
        layer.position.y = MathUtils.lerp(layer.startPosition.y, layer.targetPosition.y, progress);
      }

      if (progress >= 1f) {
        scalingBackground = false;
      }
    }
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
      Vector2 layerPos = getPosition(layer, cameraPos, position);
      updateWeather(layer);

      float layerX = layerPos.x;
      float layerY = layerPos.y;
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
        drawHorizontalCopies(batch, layer, layerX, layerY);
      } else if (layer.repeat == RepeatMode.CHAOTIC) {
        drawRain(batch, layer);
      }
      batch.setColor(prevColor);
    }
  }

  private void drawHorizontalCopies(
      SpriteBatch batch, ParallaxLayer layer, float layerX, float layerY) {
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

  private void initialiseRainOffsets(
      float startX, float endX, float startY, float endY, float gap, float verticalOffset) {

    rainOffsets = new ArrayList<>();
    int y = 0;
    float currentY = startY;
    float currentX;
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

  private static float rainRowOffset(float currentY, float gap) {
    return (int) ((currentY * (1 / gap)) % 2) == 0 ? 0 : gap / 2;
  }

  private void drawRainDrop(SpriteBatch batch, ParallaxLayer layer, float x, float y) {
    batch.draw(
        layer.texture,
        x,
        y,
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

  private void drawRain(SpriteBatch batch, ParallaxLayer layer) {
    // gap between raindrops, works best if gap * int = 1, where int is any positive integer
    float gap = 0.5f;
    // random vertical offset applied to each rain drop in range -verticalOffset to
    // verticalOffset
    float verticalOffset = 0.05f;
    float cameraWidth = truncate(camera.getCamera().viewportWidth) + 4;
    float cameraHeight = truncate(camera.getCamera().viewportHeight) + 6;
    int centerX = (int) camera.getCamera().position.x;
    int centerY = (int) camera.getCamera().position.y;
    float startX = centerX - (cameraWidth / 2);
    float endX = centerX + (cameraWidth / 2);
    float startY = centerY + (cameraHeight / 2);
    float endY = centerY - (cameraHeight / 2);
    float currentX;
    float currentY;
    float drawPosX;
    float drawPosY;

    if (rainOffsets == null) {
      initialiseRainOffsets(startX, endX, startY, endY, gap, verticalOffset);
    }

    int lengthY = rainOffsets.size();
    int lengthX = rainOffsets.getFirst().size();

    batch.setColor(0.575f, 0.575f, 1f, 0.5f);

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

        float offset = rainOffsets.get(randomOffsetY).get(randomOffsetX);
        float rowOffset = rainRowOffset(currentY, gap);
        drawRainDrop(batch, layer, drawPosX + rowOffset + offset, drawPosY + offset);
        currentX += gap;
        // Safety check for when map is activated
        randomOffsetX = Math.min(randomOffsetX + 1, lengthX - 1);
      }
      currentY -= gap;
      // Safety check for when map is activated
      randomOffsetY = Math.min(randomOffsetY + 1, lengthY - 1);
    }
  }

  public boolean getScalingBackground() {
    return scalingBackground;
  }

  /** Drops the fractional part of a value, rounding towards zero like an int cast. */
  private static float truncate(float value) {
    return (float) (value < 0 ? Math.ceil(value) : Math.floor(value));
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
