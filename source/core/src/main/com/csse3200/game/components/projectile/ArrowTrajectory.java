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
      out[i].set(start.x + velocity.x * t, start.y + velocity.y * t + 0.5f * gravityY * t * t);
    }
  }

  /**
   * Finds the direction to launch a projectile so that, falling under gravity, its arc passes
   * through a target point. Where two launch angles reach the target, the flatter one is used, so
   * the arrow gets there sooner.
   *
   * <p>If the target is out of reach at this speed, it returns the angle that gets closest instead
   * of failing, so a shooter too far away still lobs its shot the right way.
   *
   * @param from launch position
   * @param to point to hit
   * @param speed launch speed in world units per second
   * @param gravityY constant y acceleration (negative pulls down, 0 means aim straight at it)
   * @return unit vector to launch along
   */
  public static Vector2 launchDirection(Vector2 from, Vector2 to, float speed, float gravityY) {
    float dx = to.x - from.x;
    float dy = to.y - from.y;
    float horizontal = Math.abs(dx);
    float gravity = -gravityY;

    if (gravity <= 0f || speed <= 0f) {
      return straightAt(dx, dy);
    }
    if (horizontal < 1e-4f) {
      // Directly above or below: nothing to arc over.
      return new Vector2(0f, dy >= 0f ? 1f : -1f);
    }

    // With u = tan(launch angle), the arc reaches height dy at horizontal distance h when
    //   k * u^2 - h * u + (k + dy) = 0, where k = gravity * h^2 / (2 * speed^2).
    float k = gravity * horizontal * horizontal / (2f * speed * speed);
    float discriminant = horizontal * horizontal - 4f * k * (k + dy);
    float slope;
    if (discriminant >= 0f) {
      slope = (horizontal - (float) Math.sqrt(discriminant)) / (2f * k);
    } else {
      // Out of reach: the angle that gets highest at this distance is the closest it can come.
      slope = horizontal / (2f * k);
    }
    return new Vector2(Math.signum(dx), slope).nor();
  }

  private static Vector2 straightAt(float dx, float dy) {
    Vector2 direction = new Vector2(dx, dy);
    return direction.isZero() ? new Vector2(1f, 0f) : direction.nor();
  }
}
