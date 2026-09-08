package com.csse3200.game.components.item.weapons.bow.arrow;

import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemType;

/**
 * Concrete item representing stackable arrow ammunition in the inventory.
 * Stats and inventory slot separation are handled directly via ItemType.
 */
public class Arrow extends Item {

  public Arrow(ItemType itemType, int quantity) {
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