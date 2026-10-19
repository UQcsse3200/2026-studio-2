package com.csse3200.game.components.inventory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.extensions.GameExtension;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class InventorySlotStyleTest {
  // The original 22-by-22 bitmap: transparent corners, three-pixel border, and opaque fill.
  private static final String[] PIXEL_SHAPE = {
    "..........BB..........",
    "......BBBBBBBBBB......",
    "....BBBBBBBBBBBBBB....",
    "...BBBBBBBFFBBBBBBB...",
    "..BBBBBFFFFFFFFBBBBB..",
    "..BBBBFFFFFFFFFFBBBB..",
    ".BBBBFFFFFFFFFFFFBBBB.",
    ".BBBFFFFFFFFFFFFFFBBB.",
    ".BBBFFFFFFFFFFFFFFBBB.",
    ".BBBFFFFFFFFFFFFFFBBB.",
    "BBBFFFFFFFFFFFFFFFFBBB",
    "BBBFFFFFFFFFFFFFFFFBBB",
    ".BBBFFFFFFFFFFFFFFBBB.",
    ".BBBFFFFFFFFFFFFFFBBB.",
    ".BBBFFFFFFFFFFFFFFBBB.",
    ".BBBBFFFFFFFFFFFFBBBB.",
    "..BBBBFFFFFFFFFFBBBB..",
    "..BBBBBFFFFFFFFBBBBB..",
    "...BBBBBBBFFBBBBBBB...",
    "....BBBBBBBBBBBBBB....",
    "......BBBBBBBBBB......",
    "..........BB.........."
  };

  private InventorySlotStyle style;

  @BeforeEach
  void setUp() {
    reset(Gdx.gl);
    AtomicInteger handles = new AtomicInteger(1);
    when(Gdx.gl.glGenTexture()).thenAnswer(call -> handles.getAndIncrement());
    style = new InventorySlotStyle();
  }

  @AfterEach
  void tearDown() {
    style.dispose();
  }

  @Test
  void shouldCacheDistinctDrawablesWithStretchableCenters() {
    verify(Gdx.gl, never()).glGenTexture();
    NinePatchDrawable normal = style.getNormalBox();
    NinePatchDrawable selected = style.getSelectedBox();
    NinePatchDrawable darker = style.getDarkerBox();

    assertSame(normal, style.getNormalBox());
    assertSame(selected, style.getSelectedBox());
    assertSame(darker, style.getDarkerBox());
    assertNotSame(normal.getPatch().getTexture(), selected.getPatch().getTexture());
    assertNotSame(selected.getPatch().getTexture(), darker.getPatch().getTexture());
    assertNotSame(normal.getPatch().getTexture(), darker.getPatch().getTexture());
    verify(Gdx.gl, times(3)).glGenTexture();
    for (NinePatchDrawable drawable : List.of(normal, selected, darker)) {
      assertEquals(10f, drawable.getPatch().getLeftWidth());
      assertEquals(10f, drawable.getPatch().getRightWidth());
      assertEquals(10f, drawable.getPatch().getTopHeight());
      assertEquals(10f, drawable.getPatch().getBottomHeight());
      assertEquals(2f, drawable.getPatch().getMiddleWidth());
      assertEquals(2f, drawable.getPatch().getMiddleHeight());
    }
  }

  @Test
  void shouldPreserveEveryUploadedPixelForAllStyles() {
    List<byte[]> uploads = new ArrayList<>();
    doAnswer(
            call -> {
              ByteBuffer pixels = ((ByteBuffer) call.getArgument(8)).duplicate();
              byte[] copy = new byte[pixels.remaining()];
              pixels.get(copy);
              uploads.add(copy);
              return null;
            })
        .when(Gdx.gl)
        .glTexImage2D(
            eq(GL20.GL_TEXTURE_2D),
            eq(0),
            eq(GL20.GL_RGBA),
            eq(22),
            eq(22),
            eq(0),
            eq(GL20.GL_RGBA),
            eq(GL20.GL_UNSIGNED_BYTE),
            any());

    style.getNormalBox();
    style.getSelectedBox();
    style.getDarkerBox();

    assertEquals(3, uploads.size());
    assertArrayEquals(expectedPixels(0x121a1aff, 0x2b3d3eff), uploads.get(0));
    assertArrayEquals(expectedPixels(0x8c5423ff, 0x2b3d3eff), uploads.get(1));
    assertArrayEquals(expectedPixels(0x8c5423ff, 0x182324ff), uploads.get(2));
  }

  @Test
  void shouldDisposeOnceAndRecreateEachCachedTexture() {
    style.dispose();
    verify(Gdx.gl, never()).glDeleteTexture(anyInt());
    Texture normal = style.getNormalBox().getPatch().getTexture();
    Texture selected = style.getSelectedBox().getPatch().getTexture();
    Texture darker = style.getDarkerBox().getPatch().getTexture();
    int normalHandle = normal.getTextureObjectHandle();
    int selectedHandle = selected.getTextureObjectHandle();
    int darkerHandle = darker.getTextureObjectHandle();

    style.dispose();
    style.dispose();

    verify(Gdx.gl).glDeleteTexture(normalHandle);
    verify(Gdx.gl).glDeleteTexture(selectedHandle);
    verify(Gdx.gl).glDeleteTexture(darkerHandle);
    verify(Gdx.gl, times(3)).glDeleteTexture(anyInt());
    assertEquals(0, normal.getTextureObjectHandle());
    assertEquals(0, selected.getTextureObjectHandle());
    assertEquals(0, darker.getTextureObjectHandle());
    assertNotSame(normal, style.getNormalBox().getPatch().getTexture());
    assertNotSame(selected, style.getSelectedBox().getPatch().getTexture());
    assertNotSame(darker, style.getDarkerBox().getPatch().getTexture());
    verify(Gdx.gl, times(6)).glGenTexture();
  }

  private static byte[] expectedPixels(int border, int fill) {
    ByteBuffer pixels = ByteBuffer.allocate(22 * 22 * Integer.BYTES);
    for (String row : PIXEL_SHAPE) {
      for (char pixel : row.toCharArray()) {
        pixels.putInt(
            switch (pixel) {
              case 'B' -> border;
              case 'F' -> fill;
              default -> 0;
            });
      }
    }
    return pixels.array();
  }
}
