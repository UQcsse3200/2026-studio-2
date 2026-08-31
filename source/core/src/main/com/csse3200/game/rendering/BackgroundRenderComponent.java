package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/** Render multiple layers of a parallax background. */
public class BackgroundRenderComponent extends RenderComponent {

  /** A single parallax background layer. */
    private static class ParallaxLayer {private final Texture texture;
  private final float parallaxFactor;
        private final float width;
        private final float height;
  private final float yOffset;

        ParallaxLayer(
                Texture texture,
                float parallaxFactor,
                float width,
                float height,
                float yOffset) {

            this.texture = texture;
            this.parallaxFactor = parallaxFactor;
            this.width = width;
            this.height = height;
            this.yOffset = yOffset;
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
      String texturePath, float parallaxFactor,
            float width,
            float height, float yOffset) {

    this(
        ServiceLocator.getResourceService().getAsset(texturePath, Texture.class),
        camera,
        parallaxFactor);
  }
        Texture texture = ServiceLocator.getResourceService()
                .getAsset(texturePath, Texture.class);

        layers.add(
                new ParallaxLayer(
                        texture,
                        parallaxFactor,
                        width,
                        height,
                        yOffset
                )
        );
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
  public void addLayer(Texture texture,  float parallaxFactor,
            float width,
            float height,
            float yOffset) {

    layers.add(
                new ParallaxLayer(
                        texture,
     parallaxFactor,
  width,
                        height,
                        yOffset
                )
        );
    }

  /*** Scale is controlled individually for each layer. */
  public void scaleEntity() {
    // Layer sizes are defined when they are added.
  }

  @Override
  protected void draw(SpriteBatch batch) {

    if (layers.isEmpty()) {
            return;
        }Vector2 position = entity.getPosition();
    

    
    float cameraX = camera.getCamera().position.x;

    /*
         * Draw layers from back to front.
         *
         * Lower parallax factors move more slowly.
         * Higher parallax factors move more quickly.
         */
        for (ParallaxLayer layer : layers) {
    float backgroundX = position.x + cameraX * (1f - layer.parallaxFactor);

    
    float backgroundY = position.y+ layer.yOffset;

    batch.draw(texture, backgroundX, backgroundY, scale.x, scale.y);
  }
            batch.draw(
                    layer.texture,
                    backgroundX,
                    backgroundY,
                    layer.width,
                    layer.height
            );
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