package com.csse3200.game.components.inventory;

import com.csse3200.game.components.item.ItemType;

/** An immutable view of one physical inventory position. */
public final class InventorySlot {
  private static final InventorySlot EMPTY = new InventorySlot(null, 0);

  private final ItemType itemType;
  private final int quantity;

  private InventorySlot(ItemType itemType, int quantity) {
    this.itemType = itemType;
    this.quantity = quantity;
  }

  static InventorySlot empty() {
    return EMPTY;
  }

  static InventorySlot of(ItemType itemType, int quantity) {
    if (itemType == null || quantity <= 0) {
      throw new IllegalArgumentException("Occupied slots require an item and positive quantity");
    }
    return new InventorySlot(itemType, quantity);
  }

  /**
   * Returns the type stored in this slot.
   *
   * @return item type, or null when this slot is empty
   */
  public ItemType getItemType() {
    return itemType;
  }

  /**
   * Returns the quantity stored in this slot.
   *
   * @return item quantity, or zero when this slot is empty
   */
  public int getQuantity() {
    return quantity;
  }

  /**
   * Returns whether this slot contains no item.
   *
   * @return true when this slot is empty
   */
  public boolean isEmpty() {
    return itemType == null;
  }
}
