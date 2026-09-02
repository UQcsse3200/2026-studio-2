package com.csse3200.game.components.item;

/**
 * One stack of an item. Kind and stats come from {@link ItemType}; this object only tracks
 * quantity.
 */
public abstract class Item {
  private final ItemType itemType;
  private int quantity;

  public Item(ItemType itemType, int quantity) {
    this.itemType = itemType;
    this.quantity = quantity;
  }

  public ItemType getItemType() {
    return itemType;
  }

  public int getItemId() {
    return itemType.getId();
  }

  public String getItemName() {
    return itemType.getDisplayName();
  }

  public String getDescription() {
    return itemType.getDescription();
  }

  public int getQuantity() {
    return quantity;
  }

  public boolean removeQuantity(int amount) {
    if (amount <= 0 || quantity < amount) {
      return false;
    }

    quantity -= amount;
    return true;
  }

  public boolean addQuantity(int amount) {
    if (amount <= 0) {
      return false;
    }

    quantity += amount;
    return true;
  }
}
