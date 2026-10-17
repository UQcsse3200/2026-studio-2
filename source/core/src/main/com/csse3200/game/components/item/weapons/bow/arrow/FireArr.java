package com.csse3200.game.components.item.weapons.bow.arrow;

import com.csse3200.game.components.item.ItemType;

public class FireArr extends Arrow {
  public FireArr(int quantity) {
    super(ItemType.FIRE_ARROW, quantity);
  }

  @Override
  public float getBurnDamagePerSecond() {
    return getItemType().getBurnDamagePerSecond();
  }

  @Override
  public float getBurnTime() {
    return getItemType().getBurnTime();
  }
}
