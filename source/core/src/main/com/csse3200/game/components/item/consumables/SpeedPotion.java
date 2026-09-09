package com.csse3200.game.components.item.consumables;

import com.csse3200.game.components.item.ItemType;

public class SpeedPotion extends Consumable {

  public SpeedPotion(int quantity) {
    super(ItemType.SpeedPotion, quantity);
  }

  public float getSpeedBoost() {
    return getItemType().getSpeedBoost();
  }

  public float getDuration() {
    return getItemType().getDuration();
  }
}
