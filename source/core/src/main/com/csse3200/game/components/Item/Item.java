package com.csse3200.game.components.item;

public abstract class Item {
  public final ItemType itemType;
  public String itemName;
  public String description;
  public int quantity;

  public Item(ItemType itemType, String itemName, String description, int quantity) {
    this.itemType = itemType;
    this.itemName = itemName;
    this.description = description;
    this.quantity = quantity;
  }
}
