package com.csse3200.game.entities.configs;

import com.badlogic.gdx.graphics.Color;
import java.lang.reflect.Field;

/** Defines the configs to be loaded by TextBoxFactory */
public class TextConfig {
  // All values are default

  // positions
  public int xPos = 100;
  public int yPos = 100;

  // The box display. Stored as colour names (e.g. "Color.WHITE" or "WHITE") because these are
  // read straight from JSON, and Json can't deserialise a plain string into a Color object.
  // Use getTextColour() / getBackgroundColour() / getBorderColour() to resolve the actual Color.
  public String textColour = "Color.WHITE";
  public String backgroundColour = "Color.BLACK";
  public String borderColour = "Color.WHITE";

  // Typewriter effect characters per second
  public float charsPerSecond = 30f;

  // For dynamic box sizing
  public int maxWidth = 200;
  public int padding = 16;
  public int borderThickness = 3;

  // The text to be displayed
  public String text = "Test text";

  /**
   * Resolves a colour reference like "Color.WHITE" or "WHITE" against {@link Color}'s public static
   * fields. Falls back to {@code fallback} if the value is missing or unrecognised.
   */
  private static Color parseColor(String value, Color fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    String name = value.trim();
    int dot = name.lastIndexOf('.');
    if (dot >= 0) {
      name = name.substring(dot + 1);
    }
    try {
      Field field = Color.class.getField(name.toUpperCase());
      Object fieldValue = field.get(null);
      if (fieldValue instanceof Color) {
        return (Color) fieldValue;
      }
    } catch (ReflectiveOperationException | IllegalArgumentException e) {
      // fall through to fallback
    }
    return fallback;
  }

  public Color getTextColour() {
    return parseColor(textColour, Color.WHITE);
  }

  public Color getBackgroundColour() {
    return parseColor(backgroundColour, Color.BLACK);
  }

  public Color getBorderColour() {
    return parseColor(borderColour, Color.WHITE);
  }
}
