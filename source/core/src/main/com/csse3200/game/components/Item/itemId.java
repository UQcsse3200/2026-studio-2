package com.csse3200.game.components.Item;

public enum itemId {
  STANDARD_ARROW(1),
  ROPE_ARROW(2),
  CONSUMABLE(3);

  private int id;

  itemId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
