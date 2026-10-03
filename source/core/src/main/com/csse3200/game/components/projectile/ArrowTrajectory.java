package com.csse3200.game.components.projectile;

import com.badlogic.gdx.math.Vector2;

/** Predicts the path of a launched arrow, ignoring collisions. */
public final class ArrowTrajectory {
  private ArrowTrajectory() {}

  /**
   * Fills {@code out} with evenly time-spaced points along a launch arc: {@code p(t) = start +
   * velocity * t + 0.5 * gravity * t^2}, where the first point is at {@code t = step}.
   *
   * @param start launch position
   * @param velocity launch velocity in world units per second
   * @param gravityY constant y acceleration (negative pulls down, 0 flies straight)
   * @param step seconds between consecutive points
   * @param out preallocated points to overwrite; its length sets how many points are produced
   */
  public static void compute(
      Vector2 start, Vector2 velocity, float gravityY, float step, Vector2[] out) {
    for (int i = 0; i < out.length; i++) {
      float t = (i + 1) * step;
      out[i].set(
          start.x + velocity.x * t, start.y + velocity.y * t + 0.5f * gravityY * t * t);
    }
  }
}
