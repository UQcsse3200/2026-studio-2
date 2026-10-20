package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The game's pixel-art mouse pointer, used on every screen in place of the system arrow. */
public final class ThemedCursor {
  private static final Logger logger = LoggerFactory.getLogger(ThemedCursor.class);

  /** Pointer image. Its tip is the top-left pixel, which is the click point. */
  public static final String CURSOR_PATH = "images/ui/cursor.png";

  private static Cursor cursor;

  private ThemedCursor() {}

  /** Swaps the system pointer for the themed one. Keeps the system pointer if it can't load. */
  public static void install() {
    if (Gdx.graphics == null || Gdx.files == null) {
      return;
    }
    try {
      if (cursor == null) {
        Pixmap pixmap = new Pixmap(Gdx.files.internal(CURSOR_PATH));
        cursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        pixmap.dispose();
      }
      if (cursor != null) {
        Gdx.graphics.setCursor(cursor);
      }
    } catch (GdxRuntimeException e) {
      logger.warn("Could not load the themed cursor, using the system one: {}", e.getMessage());
    }
  }

  /** Frees the themed pointer when the game closes. */
  public static void dispose() {
    if (cursor != null) {
      cursor.dispose();
      cursor = null;
    }
  }
}
