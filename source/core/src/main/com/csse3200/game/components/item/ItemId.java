package com.csse3200.game.components.item;

/** Numeric identifiers for concrete item kinds. */
public enum ItemId {
  STANDARD_ARROW(1),
  ROPE_ARROW(2),
  CONSUMABLE(3),
  FIRE_ARROW(4),
  COLD_ARROW(5);

  private final int id;

  ItemId(int id) {
    this.id = id;
  }

  /**
   * @return the numeric id stored on an {@link Item}
   */
  public int getId() {
    return id;
  }
}
