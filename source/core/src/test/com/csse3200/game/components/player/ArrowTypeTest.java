package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ArrowTypeTest {
  private static final float FAR = ArrowType.DEADZONE_RADIUS * 3f;

  @Test
  void shouldResolveEachWedgeFromItsDirection() {
    assertEquals(ArrowType.NORMAL, ArrowType.forDirection(new Vector2(0f, FAR)));
    assertEquals(ArrowType.FIRE, ArrowType.forDirection(new Vector2(FAR, 0f)));
    assertEquals(ArrowType.COLD, ArrowType.forDirection(new Vector2(0f, -FAR)));
    assertEquals(ArrowType.POISON, ArrowType.forDirection(new Vector2(-FAR, 0f)));
  }

  @Test
  void shouldResolveDirectionsBetweenWedgeCentres() {
    assertEquals(ArrowType.NORMAL, ArrowType.forDirection(new Vector2(FAR * 0.3f, FAR)));
    assertEquals(ArrowType.FIRE, ArrowType.forDirection(new Vector2(FAR, FAR * 0.3f)));
    assertEquals(ArrowType.POISON, ArrowType.forDirection(new Vector2(-FAR, -FAR * 0.3f)));
  }

  @Test
  void shouldIgnoreDistanceOnceOutsideTheDeadzone() {
    assertEquals(ArrowType.FIRE, ArrowType.forDirection(new Vector2(FAR * 50f, 0f)));
  }

  @Test
  void shouldPointAtNothingInsideTheDeadzone() {
    assertNull(ArrowType.forDirection(new Vector2(0f, 0f)));
    assertNull(ArrowType.forDirection(new Vector2(ArrowType.DEADZONE_RADIUS - 1f, 0f)));
  }

  @Test
  void shouldPointAtNothingWithoutADirection() {
    assertNull(ArrowType.forDirection(null));
  }

  @Test
  void shouldGiveEveryTypeALabel() {
    for (ArrowType type : ArrowType.values()) {
      assertEquals(type.getLabel(), type.getLabel().trim());
    }
  }
}
