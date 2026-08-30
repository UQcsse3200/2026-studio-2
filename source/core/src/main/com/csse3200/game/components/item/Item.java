package com.csse3200.game.components.item;

public abstract class Item {
  private final ItemType itemType;
  private final int itemId;
  private String itemName;
  private String description;
  private int quantity;

  public Item(ItemType itemType, int itemId, String itemName, String description, int quantity) {
    this.itemType = itemType;
    this.itemId = itemId;
    this.itemName = itemName;
    this.description = description;
    this.quantity = quantity;
  }

  public ItemType getItemType() {
    return itemType;
  }

  public int getItemId() {
    return itemId;
  }

  public String getItemName() {
    return itemName;
  }

  public String getDescription() {
    return description;
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
