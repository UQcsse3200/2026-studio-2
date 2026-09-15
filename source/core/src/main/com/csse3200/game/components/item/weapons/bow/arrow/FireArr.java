package com.csse3200.game.components.item.weapons.bow.arrow;

import com.csse3200.game.components.item.ItemType;

public class FireArr extends Arrow {
  public FireArr(int quantity) {
    super(ItemType.FIRE_ARROW, quantity);
  }

  public float getBurnDamagePerSecond() {
    return getItemType().getBurnDamagePerSecond();
  }

  public float getBurnTime() {
    return getItemType().getBurnTime();
  }
}
