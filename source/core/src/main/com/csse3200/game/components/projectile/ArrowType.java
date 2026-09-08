package com.csse3200.game.components.projectile;

import com.badlogic.gdx.graphics.Color;

/**
 * Enum representing arrow variants using a single texture with custom color tints and gameplay
 * effects.
 */
public enum ArrowType {
  STANDARD(Color.WHITE),
  COLD(new Color(0.3f, 0.8f, 1f, 1f)), // Cyan / Ice Blue
  FIRE(new Color(1f, 0.4f, 0.1f, 1f)), // Fiery Red-Orange
  GRAPPLE(Color.LIGHT_GRAY);

  private static final String TEXTURE_PATH = "images/arrow.png";
  private final Color tintColor;

  ArrowType(Color tintColor) {
    this.tintColor = tintColor;
  }

  public String getTexturePath() {
    return TEXTURE_PATH;
  }

  public Color getTintColor() {
    return tintColor;
  }
}
