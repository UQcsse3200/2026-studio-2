package com.csse3200.game.components.projectile;

import com.badlogic.gdx.graphics.Color;

/**
 * Enum representing arrow variants using a single texture with custom color tints and gameplay
 * effects.
 */
public enum ArrowType {
  STANDARD(Color.WHITE),
  COLD(Color.BLUE),
  FIRE(Color.RED),
  GRAPPLE(Color.BROWN);

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
