package com.csse3200.game.components.item.weapons.bow.arrow;

import com.csse3200.game.components.item.ItemType;

public class ColdArr extends Arrow {
  public ColdArr(int quantity) {
    super(ItemType.ICE_ARROW, quantity);
  }

  public float getSlowSpeed() {
    return getItemType().getSlowSpeed();
  }

  public float getSlowTime() {
    return getItemType().getSlowTime();
  }
}
