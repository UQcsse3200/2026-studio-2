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

  @Override
  public int getLayer() {
    return FOREGROUND_LAYER;
  }
}
