package com.csse3200.game.components.projectile;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
