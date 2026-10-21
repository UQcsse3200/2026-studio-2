package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Camera;

/** A renderable with known bounds that can be skipped when it is outside the camera view. */
public interface CullingRenderable extends Renderable {
  /**
   * Determines whether this renderable intersects the camera's visible region.
   *
   * @param camera camera whose view is used for the visibility check
   * @return true if the renderable should be drawn
   */
  boolean isVisible(Camera camera);
}