package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Camera;

/** A renderable with known bounds that can be skipped when it is outside the camera view. */
public interface CullingRenderable extends Renderable {
  boolean isVisible(Camera camera);
}
