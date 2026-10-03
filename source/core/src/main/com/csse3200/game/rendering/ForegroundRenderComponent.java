package com.csse3200.game.rendering;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CameraComponent;

public class ForegroundRenderComponent extends BackgroundRenderComponent {

  private final int FOREGROUND_LAYER = 10;

  /**
   * Creates a multi-layer parallax background.
   *
   * @param camera camera used to calculate parallax movement
   * @param backgroundPos the position of the background
   * @param worldBounds the maximum x and y values of the world bounds
   */
  public ForegroundRenderComponent(
      CameraComponent camera, Vector2 backgroundPos, Vector2 worldBounds) {
    super(camera, backgroundPos, worldBounds);
  }

  /**
   * Calculates incrementing position for layers with non-zero velocity
   *
   * @param layer the layer to get new position for
   */
  /*
  @Override
  private void getPosUpdate(ParallaxLayer layer) {
      // Since this is called every frame, changing frame rates will change speed
      layer.position.x += layer.velocity.x * ServiceLocator.getTimeSource().getDeltaTime();
      layer.position.y += layer.velocity.y * ServiceLocator.getTimeSource().getDeltaTime();
      if (backgroundLight <= 0.075f) {
          backgroundLight = 0.075f;
      }
      if (backgroundLight > 0.075f) {
          // backgroundTime -= ServiceLocator.getTimeSource().getDeltaTime() / 1000f;
          backgroundLight = 1f - (ServiceLocator.getTimeSource().getTime() / 50000f); // 50,000
          backgroundLight = 1;
      }
      light = getDarkness();
      if (light == 1f) {
          if (backgroundLight < 0.3f) {
              backgroundLight = 0.3f;
          }
      }
      if (light > backgroundLight) {
          light = backgroundLight;
      }
  }
  */

  @Override
  public int getLayer() {
    return FOREGROUND_LAYER;
    // return 10000000;
  }
}
