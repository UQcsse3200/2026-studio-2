package com.csse3200.game.components.item.weapons;

import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemType;

public abstract class Weapons extends Item {
  protected Weapons(ItemType itemType, int quantity) {
    super(itemType, quantity);
  }

  public int getDamage() {
    return getItemType().getDamage();
  }

  public float getRange() {
    return getItemType().getRange();
  }
}
