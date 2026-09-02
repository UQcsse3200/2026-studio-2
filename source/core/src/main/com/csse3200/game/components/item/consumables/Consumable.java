package com.csse3200.game.components.item.consumables;

import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemType;

public abstract class Consumable extends Item {
  private final boolean consumedOnUse;

  public Consumable(ItemType itemType, int quantity) {
    super(itemType, quantity);
    this.consumedOnUse = true;
  }

  public int getTreatment() {
    return getItemType().getHealAmount();
  }

  public boolean useConsumable() {
    if (getQuantity() <= 0) {
      return false;
    }

    if (consumedOnUse) {
      return removeQuantity(1);
    }

    return true;
  }
}
