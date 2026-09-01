package com.csse3200.game.components.item.consumables;

import com.csse3200.game.components.item.ItemType;

/** A health potion that restores health when used. */
public class HealthPotion extends Consumable {
  public static final int HEAL_AMOUNT = ItemType.CONSUMABLE.getHealAmount();

  /**
   * @param quantity number of potions in the stack
   */
  public HealthPotion(int quantity) {
    super(ItemType.CONSUMABLE, quantity);
  }
}
