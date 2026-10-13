package com.csse3200.game.components.item.weapons.bow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

class BowChargeTest {
  private static final long FULL_DRAW_MS = (long) (BowCharge.MAX_CHARGE_SECONDS * 1000f);

  @Test
  void shouldStartWeakSoABarelyHeldShotDoesNotGoFar() {
    assertEquals(BowCharge.MIN_SPEED_FACTOR, BowCharge.speedMultiplier(0L), 1e-5f);
    assertTrue(BowCharge.speedMultiplier(0L) < 1f);
  }

  @Test
  void shouldBuildLinearlyWithTheHold() {
    float halfway = (BowCharge.MIN_SPEED_FACTOR + BowCharge.MAX_SPEED_FACTOR) / 2f;
    assertEquals(halfway, BowCharge.speedMultiplier(FULL_DRAW_MS / 2), 1e-4f);
  }

  @Test
  void shouldTopOutAtAFullDrawThatBeatsANormalShot() {
    assertEquals(BowCharge.MAX_SPEED_FACTOR, BowCharge.speedMultiplier(FULL_DRAW_MS), 1e-5f);
    assertEquals(BowCharge.MAX_SPEED_FACTOR, BowCharge.speedMultiplier(FULL_DRAW_MS * 10), 1e-5f);
    assertTrue(BowCharge.MAX_SPEED_FACTOR > 1f);
  }

  @Test
  void shouldNotBeInstantiable() throws NoSuchMethodException {
    Constructor<BowCharge> constructor = BowCharge.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    InvocationTargetException thrown =
        assertThrows(InvocationTargetException.class, constructor::newInstance);

    assertInstanceOf(IllegalStateException.class, thrown.getCause());
  }

  @Test
  void shouldNeverFireWeakerThanTheMinimumEvenIfTheClockStepsBackwards() {
    // e.g. the time source is replaced mid-draw by a level change. A negative multiplier would make
    // creating the arrow throw, since arrows must have a positive speed.
    assertEquals(BowCharge.MIN_SPEED_FACTOR, BowCharge.speedMultiplier(-5_000L), 1e-5f);
    assertEquals(BowCharge.MIN_SPEED_FACTOR, BowCharge.speedMultiplier(Long.MIN_VALUE), 1e-5f);
  }
}
