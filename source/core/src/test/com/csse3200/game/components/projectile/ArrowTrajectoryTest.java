package com.csse3200.game.components.projectile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

class ArrowTrajectoryTest {
  private static Vector2[] points(int count) {
    Vector2[] points = new Vector2[count];
    for (int i = 0; i < count; i++) {
      points[i] = new Vector2();
    }
    return points;
  }

  @Test
  void withoutGravityTheArrowFliesStraight() {
    Vector2[] out = points(3);
    ArrowTrajectory.compute(new Vector2(1f, 2f), new Vector2(10f, 0f), 0f, 0.5f, out);

    assertEquals(6f, out[0].x, 1e-5f);
    assertEquals(11f, out[1].x, 1e-5f);
    assertEquals(16f, out[2].x, 1e-5f);
    assertEquals(2f, out[2].y, 1e-5f);
  }

  @Test
  void gravityBendsThePathDownwardsQuadratically() {
    Vector2[] out = points(2);
    ArrowTrajectory.compute(new Vector2(0f, 0f), new Vector2(0f, 10f), -20f, 1f, out);

    // y(1) = 10 - 10 = 0, y(2) = 20 - 40 = -20
    assertEquals(0f, out[0].y, 1e-5f);
    assertEquals(-20f, out[1].y, 1e-5f);
  }

  @Test
  void overwritesAllProvidedPointsAndLeavesStartUntouched() {
    Vector2 start = new Vector2(3f, 4f);
    Vector2[] out = points(4);
    ArrowTrajectory.compute(start, new Vector2(1f, 1f), -2f, 0.1f, out);

    assertEquals(new Vector2(3f, 4f), start);
    assertEquals(3.4f, out[3].x, 1e-5f);
  }

  /** How high the arc is above or below its start once it has covered dx horizontally. */
  private static float heightAtDistance(Vector2 direction, float speed, float gravityY, float dx) {
    float time = dx / (direction.x * speed);
    return direction.y * speed * time + 0.5f * gravityY * time * time;
  }

  @Test
  void launchDirectionMakesTheArcPassThroughTheTarget() {
    float speed = 18f;
    float gravityY = -20f;
    Vector2 from = new Vector2(0f, 1f);
    for (Vector2 to :
        new Vector2[] {
          new Vector2(6f, 1f), // same height
          new Vector2(6f, 3.5f), // above
          new Vector2(6f, -4f), // below
          new Vector2(12f, 1f), // far
          new Vector2(-6f, 1f), // behind it, to the left
          new Vector2(-10f, -3f)
        }) {
      Vector2 direction = ArrowTrajectory.launchDirection(from, to, speed, gravityY);

      assertEquals(1f, direction.len(), 1e-4f);
      assertEquals(
          to.y - from.y,
          heightAtDistance(direction, speed, gravityY, to.x - from.x),
          1e-3f,
          "arc towards " + to);
    }
  }

  @Test
  void launchDirectionPrefersTheFlatterOfTwoAngles() {
    Vector2 direction =
        ArrowTrajectory.launchDirection(new Vector2(0f, 0f), new Vector2(6f, 0f), 18f, -20f);

    // The other solution is the high lob, which would be well above 45 degrees.
    assertTrue(direction.y < direction.x);
    assertTrue(direction.y > 0f);
  }

  @Test
  void launchDirectionAimsSteeperAsTheTargetGetsFurther() {
    Vector2 near =
        ArrowTrajectory.launchDirection(new Vector2(0f, 0f), new Vector2(4f, 0f), 18f, -20f);
    Vector2 far =
        ArrowTrajectory.launchDirection(new Vector2(0f, 0f), new Vector2(14f, 0f), 18f, -20f);

    assertTrue(far.angleDeg() > near.angleDeg());
  }

  @Test
  void launchDirectionGetsAsCloseAsItCanWhenTheTargetIsOutOfReach() {
    // Slow enough that 30 units is unreachable: even a perfect shot only lands about 16 away.
    Vector2 direction =
        ArrowTrajectory.launchDirection(new Vector2(0f, 0f), new Vector2(30f, 0f), 18f, -20f);

    assertEquals(1f, direction.len(), 1e-4f);
    float best = heightAtDistance(direction, 18f, -20f, 30f);
    // No other angle gets the arc higher by the time it is that far across.
    for (float degrees = 5f; degrees < 85f; degrees += 5f) {
      Vector2 other = new Vector2(1f, 0f).rotateDeg(degrees);
      assertTrue(
          best >= heightAtDistance(other, 18f, -20f, 30f) - 1e-3f,
          degrees + " degrees gets higher than the chosen " + direction.angleDeg());
    }
  }

  @Test
  void launchDirectionFlipsHorizontallyForATargetBehind() {
    Vector2 right =
        ArrowTrajectory.launchDirection(new Vector2(0f, 0f), new Vector2(6f, 2f), 18f, -20f);
    Vector2 left =
        ArrowTrajectory.launchDirection(new Vector2(0f, 0f), new Vector2(-6f, 2f), 18f, -20f);

    assertEquals(right.x, -left.x, 1e-5f);
    assertEquals(right.y, left.y, 1e-5f);
  }

  @Test
  void launchDirectionAimsStraightAtTheTargetWithoutGravity() {
    Vector2 direction =
        ArrowTrajectory.launchDirection(new Vector2(1f, 1f), new Vector2(4f, 5f), 18f, 0f);

    assertEquals(0.6f, direction.x, 1e-5f);
    assertEquals(0.8f, direction.y, 1e-5f);
  }

  @Test
  void launchDirectionShootsStraightUpOrDownAtATargetDirectlyAboveOrBelow() {
    Vector2 up =
        ArrowTrajectory.launchDirection(new Vector2(2f, 0f), new Vector2(2f, 5f), 18f, -20f);
    Vector2 down =
        ArrowTrajectory.launchDirection(new Vector2(2f, 0f), new Vector2(2f, -5f), 18f, -20f);

    assertEquals(new Vector2(0f, 1f), up);
    assertEquals(new Vector2(0f, -1f), down);
  }

  @Test
  void launchDirectionStillReturnsADirectionWhenAlreadyOnTheTarget() {
    Vector2 direction =
        ArrowTrajectory.launchDirection(new Vector2(2f, 2f), new Vector2(2f, 2f), 18f, 0f);

    assertEquals(1f, direction.len(), 1e-5f);
  }

  @Test
  void launchDirectionTreatsAZeroSpeedLikeNoGravityInsteadOfDividingByZero() {
    Vector2 direction =
        ArrowTrajectory.launchDirection(new Vector2(0f, 0f), new Vector2(3f, 4f), 0f, -20f);

    assertEquals(0.6f, direction.x, 1e-5f);
    assertEquals(0.8f, direction.y, 1e-5f);
  }
}
