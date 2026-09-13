package com.csse3200.game.components.item.consumables;

import com.csse3200.game.components.item.ItemType;

/** A consumable potion that makes the player invulnerable for a limited time. */
public class ShieldPotion extends Consumable {

  public ShieldPotion(int quantity) {
    super(ItemType.ShieldPotion, quantity);
  }

  public float getShieldDuration() {
    return getItemType().getShieldDuration();
  }
}