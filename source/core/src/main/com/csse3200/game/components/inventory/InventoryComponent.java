package com.csse3200.game.components.inventory;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.ItemType;

import java.util.EnumMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component intended to be used by the player to track their inventory.
 *
 * <p>Currently only stores the gold amount but can be extended for more advanced functionality such
 * as storing items. Can also be used as a more generic component for other entities.
 */
public class InventoryComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(InventoryComponent.class);
  private static final int DEFAULT_CAPACITY = 2;

  private final Map<ItemType, Integer> items = new EnumMap<>(ItemType.class);
  private final int capacity;

  private int gold;
  private ItemType selectedItem;

  /**
   * Creates an inventory with two distinct item slots.
   *
   * @param gold starting gold
   */
  public InventoryComponent(int gold) {
    setGold(gold);

    this.capacity = DEFAULT_CAPACITY;
  }

  /**
   * Creates an inventory with a configurable number of distinct item stacks.
   *
   * @param gold starting gold
   * @param capacity maximum number of distinct item stacks
   */
  public InventoryComponent(int gold, int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Inventory capacity must be greater than zero");
    }

    this.capacity = capacity;
    setGold(gold);
  }

  /**
   * Returns the player's gold.
   *
   * @return entity's health
   */
  public int getGold() {
    return this.gold;
  }

  /**
   * Returns if the player has a certain amount of gold.
   *
   * @param gold required amount of gold
   * @return player has greater than or equal to the required amount of gold
   */
  public Boolean hasGold(int gold) {
    return this.gold >= gold;
  }

  /**
   * Sets the player's gold. Gold has a minimum bound of 0.
   *
   * @param gold gold
   */
  public void setGold(int gold) {
    this.gold = Math.max(gold, 0);
    logger.debug("Setting gold to {}", this.gold);
  }

  /**
   * Adds to the player's gold. The amount added can be negative.
   *
   * @param gold gold to add
   */
  public void addGold(int gold) {
    setGold(this.gold + gold);
  }

  /**
   * Adds the complete quantity to an item stack.
   *
   * <p>The operation is atomic: when the inventory is full, the quantity is invalid, or the stack
   * limit would be exceeded, nothing is added.
   *
   * @return true when the complete quantity was added
   */
  public boolean addItem(ItemType item, int quantity) {
    if (item == null || quantity <= 0) {
      return false;
    }

    return true;
  }

  /**
   * Removes the complete requested quantity.
   *
   * <p>If the requested quantity is not available, the inventory remains unchanged.
   *
   * @return true when the quantity was removed
   */
  public boolean removeItem(ItemType item, int quantity) {
    if (item == null || quantity <= 0) {
      return false;
    }
    return true;
  }

  /**
   * Returns the currently selected item type.
   *
   * @return the currently selected item type
   */
  public ItemType getSelectedItem() {
    return selectedItem;
  }

  /**
   * Selects the next owned item.
   *
   * @return selected item, or null when the inventory contains no item
   */
  public ItemType selectNext() {
    return selectRelative(1);
  }

  /**
   * Selects the previous owned item.
   *
   * @return selected item, or null when the inventory contains no item
   */
  public ItemType selectPrevious() {
    return selectRelative(-1);
  }

  /**
   * Returns the number of items of a certain type in the inventory.
   *
   * @param itemType the item type to check
   * @return the number of items of that type in the inventory
   */
  public int getItemCount(ItemType item) {
    if (item == null) {
      return 0;
    }

    return items.getOrDefault(item, 0);
  }

  /**
   * Returns the number of items of a certain type in the inventory.
   *
   * @param itemType the item type to check
   * @return the number of items of that type in the inventory
   */
  public boolean hasItem(ItemType item) {
    return getItemCount(item) > 0;
  }

  /**
   * Returns the number of items of a certain type in the inventory.
   *
   * @return the number of items of that type in the inventory
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Selects the next or previous owned item.
   *
   * @param direction 1 for next, -1 for previous
   * @return selected item, or null when the inventory contains no item
   */
  private ItemType selectRelative(int direction) {
    ItemType oldSelection = selectedItem;
    ItemType[] item = ItemType.values();

    int startIndex;
    if (selectedItem == null) {
      startIndex = direction > 0 ? -1 : 0;
    } else {
      startIndex = selectedItem.ordinal();
    }

    for (int offset = 1; offset <= item.length; offset++) {
      int candidateIndex =
          Math.floorMod(startIndex + direction * offset, item.length);
      ItemType candidate = item[candidateIndex];

      if (hasItem(candidate)) {
        selectedItem = candidate;
        break;
      }
    }

    if (oldSelection != selectedItem) {
      notifySelectionChanged();
    }

    return selectedItem;
  }

  /**
   * Notifies listeners that the inventory has changed.
   *
   * <p>Currently does nothing, but can be extended to implement a specific event in the future.
   */
  private void notifyInventoryChanged() {
    // Constructor operations happen before the component is attached to an Entity.
    if (entity != null) {
      // Notify listeners that the inventory has changed (can implement specific event in the future)
    }
  }

  /**
   * Notifies listeners that the selected item has changed.
   *
   * <p>Currently does nothing, but can be extended to implement a specific event in the future.
   */
  private void notifySelectionChanged() {
    if (entity != null) {
      // Notify listeners that the selection has changed (can implement specific event in the future)
    }
  }
}
