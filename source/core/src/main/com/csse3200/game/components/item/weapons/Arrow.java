package com.csse3200.game.components.item.weapons;

import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemType;

public abstract class Arrow extends Item {
  private int damage;
  private float range;
  private boolean consumeAmmo; // Whether consume ammo
  private float cooldown; // The cooldown time;

  /**
   * Each arrow subclass supplies its own item type, so different arrows occupy separate inventory
   * slots rather than stacking together.
   */
  protected Arrow(
      ItemType itemType,
      int itemId,
      String itemName,
      String description,
      int quantity,
      int damage,
      float range,
      boolean consumeAmmo,
      float cooldown) {
    super(itemType, itemId, itemName, description, quantity);
    this.damage = damage;
    this.range = range;
    this.consumeAmmo = consumeAmmo;
    this.cooldown = cooldown;
  }

  public int getDamage() {
    return damage;
  }

  public float getRange() {
    return range;
  }

  public boolean isConsumeAmmo() {
    return consumeAmmo;
  }

  public float getCooldown() {
    return cooldown;
  }
}
