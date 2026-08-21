package com.csse3200.game.components;

public abstract class Item {
  public String itemName;
  public String description;
  public int quantity;

  public Item(String itemName, String description, int quantity) {
    this.itemName = itemName;
    this.description = description;
    this.quantity = quantity;
  }
}
