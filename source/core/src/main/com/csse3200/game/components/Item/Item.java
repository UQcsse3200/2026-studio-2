package com.csse3200.game.components.item;

public abstract class Item {
  public final ItemType itemType;
  public final int itemId;
  public String itemName;
  public String description;
  public int quantity;

  public Item(ItemType itemType, int itemId, String itemName, String description, int quantity) {
    this.itemType = itemType;
    this.itemId = itemId;
    this.itemName = itemName;
    this.description = description;
    this.quantity = quantity;
  }
}
