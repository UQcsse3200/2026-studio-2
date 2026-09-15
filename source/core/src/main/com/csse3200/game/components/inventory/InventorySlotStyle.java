package com.csse3200.game.components.inventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

/**
 * Shared rounded-box slot background used by the inventory bar and backpack, so both draw slots
 * with the same style instead of relying on an image.
 */
final class InventorySlotStyle {

  private static final int CORNER_RADIUS = 10;
  private static final int BORDER_THICKNESS = 3;

  /** Dark green/teal matching the health bar's backing art. */
  private static final Color FILL_COLOR = new Color(43 / 255f, 61 / 255f, 62 / 255f, 1f);

  private static final Color NORMAL_BORDER_COLOR = new Color(18 / 255f, 26 / 255f, 26 / 255f, 1f);

  /** Border turns brown when a slot is selected. */
  private static final Color SELECTED_BORDER_COLOR = new Color(0.55f, 0.33f, 0.14f, 1f);

  private static NinePatchDrawable cachedNormalBox;
  private static NinePatchDrawable cachedSelectedBox;

  private InventorySlotStyle() {}

  static NinePatchDrawable getNormalBox() {
    if (cachedNormalBox == null) {
      cachedNormalBox = buildRoundedBoxDrawable(NORMAL_BORDER_COLOR);
    }
    return cachedNormalBox;
  }

  static NinePatchDrawable getSelectedBox() {
    if (cachedSelectedBox == null) {
      cachedSelectedBox = buildRoundedBoxDrawable(SELECTED_BORDER_COLOR);
    }
    return cachedSelectedBox;
  }

  /**
   * Builds a rounded-rectangle NinePatch: a solid fill with a border, rounded corners, that can be
   * stretched to any slot size without distorting the corners.
   *
   * @param borderColor the border colour to draw
   * @return the built drawable
   */
  private static NinePatchDrawable buildRoundedBoxDrawable(Color borderColor) {
    int r = CORNER_RADIUS;
    int size = r * 2 + 2; // corners plus a 2px stretchable sliver in the middle

    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        boolean inCornerBox = (x < r || x >= size - r) && (y < r || y >= size - r);

        if (inCornerBox) {
          float cx = x < r ? r : size - r - 1;
          float cy = y < r ? r : size - r - 1;
          double dist = Math.hypot(x - cx, y - cy);

          if (dist > r) {
            pixmap.drawPixel(x, y, Color.rgba8888(0f, 0f, 0f, 0f));
          } else if (dist > r - BORDER_THICKNESS) {
            pixmap.drawPixel(x, y, Color.rgba8888(borderColor));
          } else {
            pixmap.drawPixel(x, y, Color.rgba8888(FILL_COLOR));
          }
        } else {
          boolean onBorder =
              x < BORDER_THICKNESS
                  || x >= size - BORDER_THICKNESS
                  || y < BORDER_THICKNESS
                  || y >= size - BORDER_THICKNESS;
          pixmap.drawPixel(x, y, Color.rgba8888(onBorder ? borderColor : FILL_COLOR));
        }
      }
    }

    Texture texture = new Texture(pixmap);
    pixmap.dispose();

    NinePatch patch = new NinePatch(texture, r, r, r, r);
    return new NinePatchDrawable(patch);
  }
}
