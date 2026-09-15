package com.csse3200.game.components.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/** Enum representing arrow variants used by the bow, projectiles, and arrow wheel. */
public enum ArrowType {
  STANDARD("Standard", Color.WHITE),
  FIRE("Fire", Color.RED),
  ICE("Ice", Color.BLUE),
  GRAPPLE("Grapple", Color.BROWN),
  POISON("Poison", Color.PURPLE);

  /** Pointer distance from the wheel centre below which nothing is pointed at. */
  public static final float DEADZONE_RADIUS = 40f;

  private static final float DEADZONE_RADIUS_SQ = DEADZONE_RADIUS * DEADZONE_RADIUS;
  private static final ArrowType[] WHEEL_TYPES = {STANDARD, FIRE, ICE, POISON};

  private final String label;
  private final Color tintColor;

  ArrowType(String label, Color tintColor) {
    this.label = label;
    this.tintColor = tintColor;
  }

  public String getLabel() {
    return label;
  }

  public String getTexturePath() {
    return switch (this) {
      case FIRE -> "images/fireArr_animation.png";
      case ICE -> "images/coldArr_animation.png";
      default -> "images/arrow.png";
    };
  }

  public Color getTintColor() {
    return tintColor;
  }

  /** Returns the arrow type the pointer is aimed at, or null inside the centre deadzone. */
  public static ArrowType forDirection(Vector2 offsetFromCentre) {
    if (offsetFromCentre == null || offsetFromCentre.len2() < DEADZONE_RADIUS_SQ) {
      return null;
    }

    float wedgeDegrees = 360f / WHEEL_TYPES.length;
    float clockwiseFromTop = (90f - offsetFromCentre.angleDeg() + 360f) % 360f;
    return WHEEL_TYPES[Math.round(clockwiseFromTop / wedgeDegrees) % WHEEL_TYPES.length];
  }
}
