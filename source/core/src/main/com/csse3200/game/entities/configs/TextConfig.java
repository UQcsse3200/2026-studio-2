package com.csse3200.game.entities.configs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.TextBoxComponent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Defines the configs to be loaded by TextBoxFactory */
public class TextConfig {
  private static final Logger logger = LoggerFactory.getLogger(TextBoxComponent.class);
  // All values are default

  // Positions
  public int xPos = 100;
  public int yPos = 100;

  // The box display. Stored as colour names (e.g. "Color.WHITE" or "WHITE") because these are
  // read straight from JSON, and Json can't deserialise a plain string into a Colour object.
  // Use getTextColour() / getBackgroundColour() / getBorderColour() to resolve the actual Colour.
  // Defaults give a scroll look: dark ink text on tan parchment with brown wooden rollers.
  public String textColour = "Color.BLACK";
  public String backgroundColour = "Color.TAN";
  public String borderColour = "Color.BROWN";

  // Typewriter effect characters per second
  public float charsPerSecond = 30f;

  // For dynamic box sizing
  public int maxWidth = 200;
  public int padding = 16;
  public int borderThickness = 3;

  // Bitmap font (.fnt), relative to assets, e.g. "flat-earth/skin/fonts/PixeloidSans.fnt".
  public String fontPath = "flat-earth/skin/fonts/PixeloidSans.fnt";

  // Horizontal alignment of the text within the box: "left", "center"/"centre", or "right".
  public String textAlignment = "center";

  // The pages of text to be displayed, in order. The box shows pages[0] first; each time the
  // fully-revealed page is dismissed (TAB), it moves on to the next entry, and only closes for
  // good after the last one.
  public List<String> pages = new ArrayList<>(List.of("Test text"));

  public Color getTextColour() {
    return parseColor(textColour, Color.BLACK);
  }

  public Color getBackgroundColour() {
    return parseColor(backgroundColour, Color.TAN);
  }

  public Color getBorderColour() {
    return parseColor(borderColour, Color.BROWN);
  }

  public int getTextAlignment() {
    return parseAlignment(textAlignment);
  }

  /**
   * Resolves a horizontal alignment name ("left", "center"/"centre", "right") to its {@link Align}
   * constant. Falls back to {@code fallback} if the value is missing or unrecognised.
   */
  private static int parseAlignment(String value) {
    if (value == null || value.isBlank()) {
      return Align.center;
    }
    return switch (value.trim().toLowerCase()) {
      case "left" -> Align.left;
      case "right" -> Align.right;
      case "center", "centre" -> Align.center;
      default -> Align.center;
    };
  }

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

  public BitmapFont loadFont() {
    if (fontPath == null || fontPath.isBlank()) {
      return new BitmapFont(Gdx.files.internal("flat-earth/skin/fonts/PixeloidSans.fnt"));
    }
    try {
      return new BitmapFont(Gdx.files.internal(fontPath));
    } catch (Exception e) {
      logger.error("Failed to load font from {}: {}", fontPath, e.getMessage());
      return null;
    }
  }
}
