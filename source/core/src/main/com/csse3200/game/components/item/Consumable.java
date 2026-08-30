package com.csse3200.game.components.item;

public abstract class Consumable extends Item {
  private int treatment;
  private boolean consumedOnUse;

  public Consumable(int itemId, String itemName, String description, int quantity, int treatment) {
    super(ItemType.CONSUMABLE, itemId, itemName, description, quantity);
    this.treatment = treatment;
    this.consumedOnUse = true;
  }

  public int getTreatment() {
    return treatment;
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
