package com.csse3200.game.components.item.weapons.bow;

/**
 * How drawing back an arrow turns into launch speed, shared by the bow and the grapple so a held
 * shot feels the same whichever one is fired. A tap fires weakly and short-range; holding builds
 * linearly up to a full draw that outpaces a normal shot.
 */
public final class BowCharge {
  /** How long a standard draw takes to reach full power. */
  public static final float MAX_CHARGE_SECONDS = 1.0f;

  /**
   * How much further every charged shot flies than the base tuning, as a multiple of distance. The
   * single knob for arrow reach: 1.2 would mean 20% further, at every charge level, for the bow and
   * the grapple alike.
   */
  public static final float REACH_MULTIPLIER = 1.0f;

  // Distance flown grows with the square of launch speed, so speed scales by the square root of the
  // reach multiplier. Scaling speed by the reach multiplier itself would overshoot (1.2x speed is
  // 1.44x the distance).
  private static final float SPEED_SCALE = (float) Math.sqrt(REACH_MULTIPLIER);

  // Fraction of base speed a shot has at zero charge - keeps a tap-release shot weak/short-range
  // rather than firing at full power or not firing at all.
  public static final float MIN_SPEED_FACTOR = 0.3f * SPEED_SCALE;

  // Multiple of base speed a fully drawn shot has - above 1, so holding to full draw beats a normal
  // shot rather than just matching it.
  public static final float MAX_SPEED_FACTOR = 1.5f * SPEED_SCALE;

  /**
   * How many times faster the grapple draws than a standard arrow. A rate rather than its own
   * duration, so it stays proportionate if {@link #MAX_CHARGE_SECONDS} is retuned.
   */
  public static final float GRAPPLE_CHARGE_RATE = 2.5f;

  /**
   * @param elapsedMs how long the draw has been held, in milliseconds
   * @return the multiplier to apply to the arrow's base launch speed
   */
  public static float speedMultiplier(long elapsedMs) {
    float elapsedSeconds = Math.clamp(elapsedMs / 1000f, 0f, MAX_CHARGE_SECONDS);
    float chargeFraction = elapsedSeconds / MAX_CHARGE_SECONDS;
    return MIN_SPEED_FACTOR + (MAX_SPEED_FACTOR - MIN_SPEED_FACTOR) * chargeFraction;
  }

  private BowCharge() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
