package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;

/** The arrow types the player can pick from the arrow wheel. */
public enum ArrowType {
  NORMAL("Normal"),
  FIRE("Fire"),
  ICE("Ice"),
  POISON("Poison");

  /** Pointer distance from the wheel centre below which nothing is pointed at. */
  public static final float DEADZONE_RADIUS = 40f;

  private static final float DEADZONE_RADIUS_SQ = DEADZONE_RADIUS * DEADZONE_RADIUS;

  private final String label;

  ArrowType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  /** Returns the arrow type the pointer is aimed at, or null inside the centre deadzone. */
  public static ArrowType forDirection(Vector2 offsetFromCentre) {
    if (offsetFromCentre == null || offsetFromCentre.len2() < DEADZONE_RADIUS_SQ) {
      return null;
    }

    ArrowType[] all = values();
    float wedgeDegrees = 360f / all.length;
    // angleDeg() runs anticlockwise from right, so flip it to run clockwise from straight up.
    float clockwiseFromTop = (90f - offsetFromCentre.angleDeg() + 360f) % 360f;
    return all[Math.round(clockwiseFromTop / wedgeDegrees) % all.length];
  }
}
