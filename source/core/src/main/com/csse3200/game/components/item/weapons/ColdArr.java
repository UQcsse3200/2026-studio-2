package com.csse3200.game.components.item.weapons;

import com.csse3200.game.components.item.ItemType;

public class ColdArr extends Arrow {
  public ColdArr(int quantity) {
    super(ItemType.ColdArrow, quantity);
  }

  public float getSlowSpeed() {
    return getItemType().getSlowSpeed();
  }

  public float getSlowTime() {
    return getItemType().getSlowTime();
  }
}
