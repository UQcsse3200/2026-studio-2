package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ArrowTypeTest {
  private static final float FAR = ArrowType.DEADZONE_RADIUS * 3f;
  // Wedge centres for three types: top, then 120 degrees clockwise, then 240 degrees clockwise.
  private static final Vector2 TOP = new Vector2(0f, FAR);
  private static final Vector2 LOWER_RIGHT = new Vector2(0.866f * FAR, -0.5f * FAR);
  private static final Vector2 LOWER_LEFT = new Vector2(-0.866f * FAR, -0.5f * FAR);

  @Test
  void shouldResolveEachWedgeFromItsDirection() {
    assertEquals(ArrowType.STANDARD, ArrowType.forDirection(TOP));
    assertEquals(ArrowType.FIRE, ArrowType.forDirection(LOWER_RIGHT));
    assertEquals(ArrowType.ICE, ArrowType.forDirection(LOWER_LEFT));
  }

  @Test
  void shouldResolveDirectionsBetweenWedgeCentres() {
    assertEquals(ArrowType.STANDARD, ArrowType.forDirection(new Vector2(FAR * 0.5f, FAR)));
    assertEquals(ArrowType.FIRE, ArrowType.forDirection(new Vector2(FAR, 0f)));
    assertEquals(ArrowType.ICE, ArrowType.forDirection(new Vector2(-FAR, 0f)));
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

  @Test
  void shouldUseSpecialProjectileTexturesForElementalArrows() {
    assertEquals("images/fireArr_animation.png", ArrowType.FIRE.getTexturePath());
    assertEquals("images/coldArr_animation.png", ArrowType.ICE.getTexturePath());
    assertEquals("images/poison_potion.png", ArrowType.POTION.getTexturePath());
  }
}
