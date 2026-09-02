package com.csse3200.game.components.item.weapons;

import com.csse3200.game.components.item.ItemType;

public class StandardArr extends Arrow {
  public StandardArr(int quantity) {
    super(ItemType.ARROW, quantity);
  }

  public boolean useArr() {
    return removeQuantity(1);
  }
}
