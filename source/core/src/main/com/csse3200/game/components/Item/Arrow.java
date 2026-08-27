package com.csse3200.game.components.item;

public abstract class Arrow extends Item {
  protected int damage;
  protected float range;
  protected boolean consumeAmmo; // Whether consume ammo
  protected float cooldown; // The cooldown time;

  /**
   * Each arrow subclass supplies its own item type, so different arrows occupy separate inventory
   * slots rather than stacking together.
   */
  protected Arrow(
      ItemType itemType,
      String itemName,
      String description,
      int quantity,
      int damage,
      float range,
      boolean consumeAmmo,
      float cooldown) {
    super(itemType, itemName, description, quantity);
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
