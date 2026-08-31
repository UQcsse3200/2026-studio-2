package com.csse3200.game.components.item;

/** Stable identifiers for inventory item types. */
public enum ItemType {
  ARROW(ItemId.STANDARD_ARROW),
  RopeArrow(ItemId.ROPE_ARROW),
  FireArrow(ItemId.FIRE_ARROW),
  ColdArrow(ItemId.COLD_ARROW),
  CONSUMABLE(ItemId.CONSUMABLE);

  private final ItemId itemId;

  ItemType(ItemId itemId) {
    this.itemId = itemId;
  }

  /**
   * Returns the stable numeric ID for this inventory item type.
   *
   * @return numeric item ID
   */
  public int getId() {
    return itemId.getId();
  }
}
