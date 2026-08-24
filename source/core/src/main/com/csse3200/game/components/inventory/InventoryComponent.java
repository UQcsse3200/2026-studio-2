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
 * <p>Tracks the player's gold, stored items, inventory capacity, and currently selected item.
 */
public class InventoryComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(InventoryComponent.class);
  private static final int DEFAULT_CAPACITY = 2;

  private final Map<ItemType, Integer> storedItems = new EnumMap<>(ItemType.class);
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
   * @return the player's current gold
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
   * Adds the requested quantity of an item to the inventory.
   *
   * <p>If the quantity is invalid or there is no available slot for a new item type, the inventory
   * remains unchanged.
   *
   * @param item item type to add
   * @param quantity quantity to add
   * @return true if the item was successfully added
   */
  public boolean addItem(ItemType item, int quantity) {
    if (item == null || quantity <= 0) {
      return false;
    }
    boolean isNewItem = !storedItems.containsKey(item);

    if (isNewItem && isFull()) {
      return false;
    }

    int currentQuantity = getItemCount(item);
    storedItems.put(item, currentQuantity + quantity);

    // Automatically select the first item added.
    if (selectedItem == null) {
      selectedItem = item;
      notifySelectionChanged();
    }
    notifyInventoryChanged();
    return true;
  }

  /**
   * Removes the requested quantity of an item from the inventory.
   *
   * <p>If the requested quantity is not available, the inventory remains unchanged.
   *
   * @param item item type to remove
   * @param quantity quantity to remove
   * @return true if the item was successfully removed
   */
  public boolean removeItem(ItemType item, int quantity) {
    if (item == null || quantity <= 0) {
      return false;
    }
    int currentQuantity = getItemCount(item);

    if (currentQuantity < quantity) {
      return false;
    }

    int remainingQuantity = currentQuantity - quantity;

    if (remainingQuantity == 0) {
      storedItems.remove(item);

      if (selectedItem == item) {
        selectedItem = null;

        if (!storedItems.isEmpty()) {
          selectNext();
        } else {
          notifySelectionChanged();
        }
      }
    } else {
      storedItems.put(item, remainingQuantity);
    }

    notifyInventoryChanged();
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
   * @param item the item type to check
   * @return the number of items of that type in the inventory
   */
  public int getItemCount(ItemType item) {
    if (item == null) {
      return 0;
    }

    return storedItems.getOrDefault(item, 0);
  }

  /**
   * Checks whether the inventory contains the specified item type.
   *
   * @param item item type to check
   * @return true if at least one item of that type is stored
   */
  public boolean hasItem(ItemType item) {
    return getItemCount(item) > 0;
  }

  /**
   * Returns whether all distinct item slots are occupied.
   *
   * @return true if the inventory has no free item slots
   */
  public boolean isFull() {
    return storedItems.size() >= capacity;
  }

  /**
   * Returns the maximum number of distinct item stacks.
   *
   * @return inventory capacity
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
    if (storedItems.isEmpty()) {
      selectedItem = null;
      return null;
    }

    ItemType oldSelection = selectedItem;
    ItemType[] item = ItemType.values();

    int startIndex;
    if (selectedItem == null) {
      startIndex = direction > 0 ? -1 : 0;
    } else {
      startIndex = selectedItem.ordinal();
    }

    for (int offset = 1; offset <= item.length; offset++) {
      int candidateIndex = Math.floorMod(startIndex + direction * offset, item.length);
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
      entity.getEvents().trigger("inventoryChanged");
    }
  }

  /**
   * Notifies listeners that the selected item has changed.
   *
   * <p>Currently does nothing, but can be extended to implement a specific event in the future.
   */
  private void notifySelectionChanged() {
    if (entity != null) {
      entity.getEvents().trigger("inventorySelectionChanged");
    }
  }
}
