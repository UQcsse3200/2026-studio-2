package com.csse3200.game.rendering;

import com.badlogic.gdx.math.Frustum;

/** Shared camera-frustum checks for renderables with rectangular world bounds. */
final class RenderCulling {
  private RenderCulling() {}

  static boolean isVisible(
      Frustum frustum, float x, float y, float width, float height, float rotationDegrees) {
    float halfWidth = Math.abs(width) / 2f;
    float halfHeight = Math.abs(height) / 2f;
    double rotation = Math.toRadians(rotationDegrees);
    float cos = (float) Math.abs(Math.cos(rotation));
    float sin = (float) Math.abs(Math.sin(rotation));
    float rotatedHalfWidth = halfWidth * cos + halfHeight * sin;
    float rotatedHalfHeight = halfWidth * sin + halfHeight * cos;

    return frustum.boundsInFrustum(
        x + width / 2f, y + height / 2f, 0f, rotatedHalfWidth, rotatedHalfHeight, 0f);
  }
}
