package com.csse3200.game.components.item.weapons;

import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemType;

public abstract class Arrow extends Item {
  /**
   * Each arrow subclass supplies its own item type, so different arrows occupy separate inventory
   * slots rather than stacking together. Combat stats are read from that type.
   */
  protected Arrow(ItemType itemType, int quantity) {
    super(itemType, quantity);
  }

  public int getDamage() {
    return getItemType().getDamage();
  }

  public float getRange() {
    return getItemType().getRange();
  }

  public boolean isConsumeAmmo() {
    return getItemType().consumesAmmo();
  }

  public float getCooldown() {
    return getItemType().getCooldown();
  }
}
