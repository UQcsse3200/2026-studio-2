package com.csse3200.game.components.item.consumables;

import com.csse3200.game.components.item.ItemId;

/** A health potion that restores health when used. */
public class HealthPotion extends Consumable {
  public static final int HEAL_AMOUNT = 25;

  /**
   * @param quantity number of potions in the stack
   */
  public HealthPotion(int quantity) {
    super(
        ItemId.CONSUMABLE.getId(),
        "Health Potion",
        "Restores a small amount of health.",
        quantity,
        HEAL_AMOUNT);
  }
}
