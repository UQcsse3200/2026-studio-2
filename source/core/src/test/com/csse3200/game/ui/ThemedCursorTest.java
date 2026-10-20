package com.csse3200.game.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ThemedCursorTest {
  private Graphics graphics;
  private Cursor cursor;

  @BeforeEach
  void setUp() {
    ThemedCursor.dispose();
    graphics = mock(Graphics.class);
    cursor = mock(Cursor.class);
    when(graphics.newCursor(any(Pixmap.class), anyInt(), anyInt())).thenReturn(cursor);
    Gdx.graphics = graphics;
  }

  @AfterEach
  void tearDown() {
    ThemedCursor.dispose();
  }

  @Test
  void cursorImageShouldBeA32PixelSquareWithItsTipInTheTopLeft() {
    FileHandle file = Gdx.files.internal(ThemedCursor.CURSOR_PATH);
    assertTrue(file.exists());

    Pixmap pixmap = new Pixmap(file);
    try {
      assertEquals(32, pixmap.getWidth());
      assertEquals(32, pixmap.getHeight());
      // The click point is (0, 0), so the outline's tip must be drawn there.
      assertEquals(0xFF, pixmap.getPixel(0, 0) & 0xFF);
    } finally {
      pixmap.dispose();
    }
  }

  @Test
  void shouldSetTheThemedCursorWithTheClickPointAtTheTip() {
    ThemedCursor.install();

    verify(graphics).newCursor(any(Pixmap.class), eq(0), eq(0));
    verify(graphics).setCursor(cursor);
  }

  @Test
  void shouldReuseTheLoadedCursor() {
    ThemedCursor.install();
    ThemedCursor.install();

    verify(graphics).newCursor(any(Pixmap.class), anyInt(), anyInt());
  }

  @Test
  void shouldKeepTheSystemCursorIfTheImageIsMissing() {
    Files files = mock(Files.class);
    FileHandle missing = mock(FileHandle.class);
    when(files.internal(ThemedCursor.CURSOR_PATH)).thenReturn(missing);
    when(missing.readBytes()).thenThrow(new GdxRuntimeException("missing"));
    Files realFiles = Gdx.files;
    Gdx.files = files;
    try {
      assertDoesNotThrow(ThemedCursor::install);
      verify(graphics, never()).setCursor(any());
    } finally {
      Gdx.files = realFiles;
    }
  }

  @Test
  void crosshairImageShouldBeA32PixelSquareWithItsCentreDotAtTheClickPoint() {
    FileHandle file = Gdx.files.internal(ThemedCursor.CROSSHAIR_PATH);
    assertTrue(file.exists());

    Pixmap pixmap = new Pixmap(file);
    try {
      assertEquals(32, pixmap.getWidth());
      assertEquals(32, pixmap.getHeight());
      int hotspot = ThemedCursor.CROSSHAIR_HOTSPOT;
      assertEquals(0xFF, pixmap.getPixel(hotspot, hotspot) & 0xFF);
      assertEquals(0xFF, pixmap.getPixel(hotspot + 1, hotspot + 1) & 0xFF);
    } finally {
      pixmap.dispose();
    }
  }

  @Test
  void shouldUseTheCrosshairWithTheClickPointAtItsCentre() {
    ThemedCursor.use(ThemedCursor.Style.CROSSHAIR);

    int hotspot = ThemedCursor.CROSSHAIR_HOTSPOT;
    verify(graphics).newCursor(any(Pixmap.class), eq(hotspot), eq(hotspot));
    verify(graphics).setCursor(cursor);
    assertEquals(ThemedCursor.Style.CROSSHAIR, ThemedCursor.getCurrent());
  }

  @Test
  void shouldSwitchBackToTheArrow() {
    ThemedCursor.use(ThemedCursor.Style.CROSSHAIR);
    ThemedCursor.use(ThemedCursor.Style.ARROW);

    verify(graphics).newCursor(any(Pixmap.class), eq(0), eq(0));
    assertEquals(ThemedCursor.Style.ARROW, ThemedCursor.getCurrent());
  }

  @Test
  void shouldFreeTheCursorOnDispose() {
    ThemedCursor.install();
    ThemedCursor.dispose();

    verify(cursor).dispose();
    assertNull(ThemedCursor.getCurrent());
  }
}
