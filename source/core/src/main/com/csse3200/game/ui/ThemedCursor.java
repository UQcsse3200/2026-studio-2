package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The game's pixel-art mouse pointers: an arrow for menus and a crosshair for aiming, used in place
 * of the system arrow.
 */
public final class ThemedCursor {
  private static final Logger logger = LoggerFactory.getLogger(ThemedCursor.class);

  /** Arrow pointer image. Its tip is the top-left pixel, which is the click point. */
  public static final String CURSOR_PATH = "images/ui/cursor.png";

  /** Crosshair image. Its centre dot is the click point. */
  public static final String CROSSHAIR_PATH = "images/ui/crosshair.png";

  /** Click point of the crosshair: the middle of its 2x2 centre dot. */
  static final int CROSSHAIR_HOTSPOT = 15;

  /** Which themed pointer is showing. */
  public enum Style {
    ARROW,
    CROSSHAIR
  }

  private static Cursor arrow;
  private static Cursor crosshair;
  private static Style current;

  private ThemedCursor() {}

  /** Swaps the system pointer for the themed arrow. Keeps the system pointer if it can't load. */
  public static void install() {
    use(Style.ARROW);
  }

  /**
   * Shows the given themed pointer, loading it the first time. Does nothing if it is already
   * showing or can't load.
   *
   * @param style the pointer to show
   */
  public static void use(Style style) {
    if (style == current || Gdx.graphics == null || Gdx.files == null) {
      return;
    }
    Cursor cursor = style == Style.CROSSHAIR ? getCrosshair() : getArrow();
    if (cursor != null) {
      Gdx.graphics.setCursor(cursor);
      current = style;
    }
  }

  /**
   * @return the themed pointer currently showing, or null if none has been set
   */
  public static Style getCurrent() {
    return current;
  }

  /** Frees the themed pointers when the game closes. */
  public static void dispose() {
    if (arrow != null) {
      arrow.dispose();
      arrow = null;
    }
    if (crosshair != null) {
      crosshair.dispose();
      crosshair = null;
    }
    current = null;
  }

  private static Cursor getArrow() {
    if (arrow == null) {
      arrow = load(CURSOR_PATH, 0, 0);
    }
    return arrow;
  }

  private static Cursor getCrosshair() {
    if (crosshair == null) {
      crosshair = load(CROSSHAIR_PATH, CROSSHAIR_HOTSPOT, CROSSHAIR_HOTSPOT);
    }
    return crosshair;
  }

  private static Cursor load(String path, int hotspotX, int hotspotY) {
    try {
      Pixmap pixmap = new Pixmap(Gdx.files.internal(path));
      Cursor cursor = Gdx.graphics.newCursor(pixmap, hotspotX, hotspotY);
      pixmap.dispose();
      return cursor;
    } catch (GdxRuntimeException e) {
      logger.warn(
          "Could not load the themed cursor {}, using the system one: {}", path, e.getMessage());
      return null;
    }
  }
}
