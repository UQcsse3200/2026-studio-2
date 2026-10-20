package com.csse3200.game.entities.configs;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import java.lang.reflect.Field;
import java.util.Locale;

/**
 * One step of a tutorial sequence, read from a tutorial config's "steps" array. A step completes
 * when any of its listed inputs is seen, or when every listed input has been seen if {@link
 * #requireAll} is set. Each step pairs with the page at the same index in {@link TextConfig#pages}.
 */
public class TutorialStepConfig {
  // libGDX key names, e.g. "SPACE", "SHIFT_LEFT", "TAB" (fields of com.badlogic.gdx.Input.Keys).
  public String[] keys = {};

  // Mouse button names ("LEFT", "RIGHT", "MIDDLE") counted when the button is pressed.
  public String[] mouse = {};

  // Mouse button names counted when the button is released, e.g. letting an arrow fly.
  public String[] mouseUp = {};

  // When true, every listed input must be seen before the step completes; otherwise any one does.
  public boolean requireAll = false;

  /**
   * Resolves {@link #keys} to libGDX keycodes.
   *
   * @throws IllegalArgumentException if a name does not match a field of {@link Keys}
   */
  public int[] resolveKeyCodes() {
    int[] codes = new int[lengthOf(keys)];
    for (int i = 0; i < codes.length; i++) {
      codes[i] = keyCode(normalise(keys[i], "key"));
    }
    return codes;
  }

  /** Resolves {@link #mouse} to libGDX button codes. */
  public int[] resolvePressedButtons() {
    return resolveButtons(mouse);
  }

  /** Resolves {@link #mouseUp} to libGDX button codes. */
  public int[] resolveReleasedButtons() {
    return resolveButtons(mouseUp);
  }

  private static int[] resolveButtons(String[] names) {
    int[] codes = new int[lengthOf(names)];
    for (int i = 0; i < codes.length; i++) {
      codes[i] = buttonCode(normalise(names[i], "mouse button"));
    }
    return codes;
  }

  private static String normalise(String name, String kind) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("empty " + kind + " name");
    }
    return name.trim().toUpperCase(Locale.ROOT);
  }

  private static int buttonCode(String name) {
    return switch (name) {
      case "LEFT" -> Buttons.LEFT;
      case "RIGHT" -> Buttons.RIGHT;
      case "MIDDLE" -> Buttons.MIDDLE;
      default -> throw new IllegalArgumentException("unknown mouse button: " + name);
    };
  }

  /**
   * Resolves a key name to its keycode by matching a field of {@link Keys}. Reflection is used
   * rather than {@code Keys.valueOf} because the latter silently returns -1 for some valid names.
   */
  private static int keyCode(String name) {
    try {
      Field field = Keys.class.getField(name);
      if (field.getType() == int.class) {
        return field.getInt(null);
      }
    } catch (ReflectiveOperationException | IllegalArgumentException e) {
      // fall through to the error below
    }
    throw new IllegalArgumentException("unknown key: " + name);
  }

  private static int lengthOf(String[] names) {
    return names == null ? 0 : names.length;
  }
}
