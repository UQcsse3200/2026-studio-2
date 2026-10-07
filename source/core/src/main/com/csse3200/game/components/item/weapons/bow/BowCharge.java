package com.csse3200.game.components.item.weapons.bow;

/**
 * How drawing back an arrow turns into launch speed, shared by the bow and the grapple so a held
 * shot feels the same whichever one is fired. A tap fires weakly and short-range; holding builds
 * linearly up to a full draw that outpaces a normal shot.
 */
public final class BowCharge {
  /** How long a draw takes to reach full power. */
  public static final float MAX_CHARGE_SECONDS = 1.5f;

  // Fraction of base speed a shot has at zero charge - keeps a tap-release shot weak/short-range
  // rather than firing at full power or not firing at all.
  public static final float MIN_SPEED_FACTOR = 0.3f;

  // Multiple of base speed a fully drawn shot has - above 1, so holding to full draw beats a normal
  // shot rather than just matching it.
  public static final float MAX_SPEED_FACTOR = 1.5f;

  /**
   * @param elapsedMs how long the draw has been held, in milliseconds
   * @return the multiplier to apply to the arrow's base launch speed
   */
  public static float speedMultiplier(long elapsedMs) {
    float elapsedSeconds = Math.max(0f, Math.min(MAX_CHARGE_SECONDS, elapsedMs / 1000f));
    float chargeFraction = elapsedSeconds / MAX_CHARGE_SECONDS;
    return MIN_SPEED_FACTOR + (MAX_SPEED_FACTOR - MIN_SPEED_FACTOR) * chargeFraction;
  }

  private BowCharge() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
