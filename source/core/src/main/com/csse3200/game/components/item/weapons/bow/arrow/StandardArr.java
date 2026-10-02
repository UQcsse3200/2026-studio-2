package com.csse3200.game.components.item.weapons.bow.arrow;

import com.csse3200.game.components.item.ItemType;

public class StandardArr extends Arrow {
  public StandardArr(int quantity) {
    super(ItemType.STANDARD_ARROW, quantity);
  }

  public boolean useArr() {
    return removeQuantity(1);
  }
}
