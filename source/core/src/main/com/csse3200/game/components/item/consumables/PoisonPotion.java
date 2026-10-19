package com.csse3200.game.components.item.consumables;

import com.csse3200.game.components.item.ItemType;

public class PoisonPotion extends Consumable {

  public PoisonPotion(int quantity) {
    super(ItemType.POISON_POTION, quantity);
  }

  public float getPoisonDamage() {
    return getItemType().getPoisonDamagePerSecond();
  }

  public float getDuration() {
    return getItemType().getPoisonDuration();
  }
}
