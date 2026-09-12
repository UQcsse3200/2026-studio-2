package com.csse3200.game.components.inventory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.components.item.ItemType;

/** Inventory images with transparent source padding removed and aspect ratio preserved. */
final class ItemIcon {
  private ItemIcon() {}

  static Image create(ItemType type, Texture texture) {
    // arrow.png is a 100x100 canvas; the visible shaft occupies only these pixels.
    TextureRegion region = type == ItemType.ARROW
        ? new TextureRegion(texture, 39, 43, 26, 7) : new TextureRegion(texture);
    Image image = new Image(region);
    image.setScaling(Scaling.fit);
    return image;
  }
}
