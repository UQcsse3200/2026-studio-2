package com.csse3200.game.components.inventory;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.ItemType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component intended to be used by the player to track their inventory.
 *
 * <p>Tracks the player's gold, stored items, inventory capacity, and currently selected item.
 */
public class InventoryComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(InventoryComponent.class);
  private static final int DEFAULT_ROWS = 3;
  private static final int DEFAULT_COLUMNS = 8;
  private static final int MAX_COLUMNS = 9;

  private final List<InventorySlot> slots;
  private int rows;
  private int columns;

  private int gold;
  private int selectedSlotIndex = -1;

  /**
   * Creates an inventory with the default capacity.
   *
   * @param gold starting gold
   */
  public InventoryComponent(int gold) {
    this(gold, DEFAULT_ROWS, DEFAULT_COLUMNS);
  }

  /**
   * Creates a single-row inventory with a configurable number of distinct item stacks.
   *
   * @param gold starting gold
   * @param capacity number of slots in the single row, from 1 to 9
   */
  public InventoryComponent(int gold, int capacity) {
    this(gold, 1, capacity);
  }

  /**
   * Creates an inventory with configurable grid dimensions.
   *
   * <p>The first row is the hotbar. Columns are limited to nine so every hotbar slot has a number
   * key.
   *
   * @param gold starting gold
   * @param rows number of inventory rows
   * @param columns number of slots per row, from 1 to 9
   */
  public InventoryComponent(int gold, int rows, int columns) {
    long capacity = (long) rows * columns;
    if (rows <= 0 || columns <= 0 || columns > MAX_COLUMNS || capacity > Integer.MAX_VALUE) {
      throw new IllegalArgumentException(
          "Inventory rows must be positive, columns must be between 1 and 9, and total slots must "
              + "not exceed Integer.MAX_VALUE");
    }

    this.rows = rows;
    this.columns = columns;
    this.slots = new ArrayList<>((int) capacity);
    for (int index = 0; index < capacity; index++) {
      slots.add(InventorySlot.empty());
    }
    setGold(gold);
  }

  @Override
  public void create() {
    entity.getEvents().addListener("selectQuickSlot", this::selectQuickSlot);
  }

  // ---------
  // Gold
  // ---------

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

  // ---------
  // Add/remove items
  // ---------

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

    if (item == ItemType.RopeArrow && hasItem(ItemType.RopeArrow)) {
      return false;
    }

    int oldSelectedSlotIndex = selectedSlotIndex;
    ItemType oldSelectedItem = getSelectedItem();
    int slotIndex = findItemSlot(item);
    if (slotIndex < 0) {
      slotIndex = findEmptySlot();
      if (slotIndex < 0) {
        return false;
      }
      slots.set(slotIndex, InventorySlot.of(item, quantity));
    } else {
      InventorySlot slot = slots.get(slotIndex);
      long newQuantity = (long) slot.getQuantity() + quantity;
      if (newQuantity > Integer.MAX_VALUE) {
        return false;
      }
      slots.set(slotIndex, InventorySlot.of(item, (int) newQuantity));
    }

    if (selectedSlotIndex < 0) {
      selectedSlotIndex = slotIndex;
    }
    notifySelectionChangedIfNeeded(oldSelectedSlotIndex, oldSelectedItem);
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
    int slotIndex = findItemSlot(item);
    int currentQuantity = slotIndex < 0 ? 0 : slots.get(slotIndex).getQuantity();

    if (currentQuantity < quantity) {
      return false;
    }

    int oldSelectedSlotIndex = selectedSlotIndex;
    ItemType oldSelectedItem = getSelectedItem();
    int remainingQuantity = currentQuantity - quantity;

    if (remainingQuantity == 0) {
      slots.set(slotIndex, InventorySlot.empty());

      if (selectedSlotIndex == slotIndex) {
        selectedSlotIndex = findRelativeOccupiedSlot(slotIndex, 1);
      }
    } else {
      slots.set(slotIndex, InventorySlot.of(item, remainingQuantity));
    }

    notifySelectionChangedIfNeeded(oldSelectedSlotIndex, oldSelectedItem);
    notifyInventoryChanged();
    return true;
  }

  // ---------
  // Selection
  // ---------

  /**
   * Returns the currently selected item type.
   *
   * @return the currently selected item type
   */
  public ItemType getSelectedItem() {
    InventorySlot selectedSlot = getSlot(selectedSlotIndex);
    return selectedSlot == null ? null : selectedSlot.getItemType();
  }

  /**
   * Returns the currently selected physical slot.
   *
   * @return zero-based slot index, or -1 when no slot is selected
   */
  public int getSelectedSlotIndex() {
    return selectedSlotIndex;
  }

  /**
   * Selects a physical inventory position, including an empty position.
   *
   * @param index zero-based slot index
   * @return true when the selected position changed
   */
  public boolean selectSlot(int index) {
    if (!isValidSlotIndex(index) || selectedSlotIndex == index) {
      return false;
    }
    selectedSlotIndex = index;
    notifySelectionChanged();
    return true;
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

  private void selectQuickSlot(int slotIndex) {
    if (slotIndex >= 0 && slotIndex < getHotbarSlotCount()) {
      selectSlot(slotIndex);
    }
  }

  // ---------
  // Queries
  // ---------

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

    int slotIndex = findItemSlot(item);
    return slotIndex < 0 ? 0 : slots.get(slotIndex).getQuantity();
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
    return findEmptySlot() < 0;
  }

  /**
   * Returns the maximum number of distinct item stacks.
   *
   * @return inventory capacity
   */
  public int getCapacity() {
    return slots.size();
  }

  /**
   * Returns the current number of inventory rows.
   *
   * @return inventory row count
   */
  public int getRows() {
    return rows;
  }

  /**
   * Returns the current number of slots in each row.
   *
   * @return inventory column count
   */
  public int getColumns() {
    return columns;
  }

  /**
   * Returns the number of slots in the first-row hotbar.
   *
   * @return hotbar slot count
   */
  public int getHotbarSlotCount() {
    return columns;
  }

  /**
   * Returns the slot at an inventory position.
   *
   * @param index zero-based slot index
   * @return immutable slot, or null when the index is invalid
   */
  public InventorySlot getSlot(int index) {
    return isValidSlotIndex(index) ? slots.get(index) : null;
  }

  /**
   * Returns an unmodifiable snapshot of every inventory slot.
   *
   * @return ordered slot snapshot
   */
  public List<InventorySlot> getSlots() {
    return Collections.unmodifiableList(new ArrayList<>(slots));
  }

  /**
   * Returns the number of physical inventory positions.
   *
   * @return slot count
   */
  public int getSlotCount() {
    return slots.size();
  }

  /**
   * Returns whether a valid inventory position is empty.
   *
   * @param index zero-based slot index
   * @return true only when the index is valid and its slot is empty
   */
  public boolean isSlotEmpty(int index) {
    return isValidSlotIndex(index) && slots.get(index).isEmpty();
  }

  // ---------
  // Slot operations
  // ---------

  /**
   * Appends one empty row without changing any existing slot index.
   *
   * @return true when the row was added, or false if the resulting capacity would overflow
   */
  public boolean addRow() {
    long newCapacity = ((long) rows + 1) * columns;
    if (newCapacity > Integer.MAX_VALUE) {
      return false;
    }

    for (int column = 0; column < columns; column++) {
      slots.add(InventorySlot.empty());
    }
    rows++;
    notifyInventoryChanged();
    return true;
  }

  /**
   * Appends one empty column while preserving every existing item's row and column coordinates.
   *
   * @return true when the column was added, or false at the nine-column limit or on overflow
   */
  public boolean addColumn() {
    if (columns >= MAX_COLUMNS) {
      return false;
    }

    int newColumns = columns + 1;
    long newCapacity = (long) rows * newColumns;
    if (newCapacity > Integer.MAX_VALUE) {
      return false;
    }

    int oldColumns = columns;
    int oldSelectedSlotIndex = selectedSlotIndex;
    ItemType oldSelectedItem = getSelectedItem();
    List<InventorySlot> expandedSlots = new ArrayList<>((int) newCapacity);
    for (int row = 0; row < rows; row++) {
      int rowStart = row * oldColumns;
      expandedSlots.addAll(slots.subList(rowStart, rowStart + oldColumns));
      expandedSlots.add(InventorySlot.empty());
    }

    slots.clear();
    slots.addAll(expandedSlots);
    columns = newColumns;
    if (oldSelectedSlotIndex >= 0) {
      int selectedRow = oldSelectedSlotIndex / oldColumns;
      int selectedColumn = oldSelectedSlotIndex % oldColumns;
      selectedSlotIndex = selectedRow * newColumns + selectedColumn;
    }

    notifyInventoryChanged();
    notifySelectionChangedIfNeeded(oldSelectedSlotIndex, oldSelectedItem);
    return true;
  }

  /**
   * Exchanges the contents of two physical inventory positions.
   *
   * @param firstIndex first zero-based slot index
   * @param secondIndex second zero-based slot index
   * @return true when two different slot values were exchanged
   */
  public boolean swapSlots(int firstIndex, int secondIndex) {
    if (!isValidSlotIndex(firstIndex)
        || !isValidSlotIndex(secondIndex)
        || firstIndex == secondIndex) {
      return false;
    }

    InventorySlot firstSlot = slots.get(firstIndex);
    InventorySlot secondSlot = slots.get(secondIndex);
    if (firstSlot == secondSlot) {
      return false;
    }

    ItemType oldSelectedItem = getSelectedItem();
    slots.set(firstIndex, secondSlot);
    slots.set(secondIndex, firstSlot);

    notifyInventoryChanged();
    if (oldSelectedItem != getSelectedItem()) {
      notifySelectionChanged();
    }
    return true;
  }

  /**
   * Sorts occupied slots by {@link ItemType} declaration order and moves empty slots to the end.
   *
   * <p>If an item is selected, that item remains selected at its new slot index. A selected empty
   * slot remains at the same physical index.
   *
   * @return true when the slot order changed
   */
  public boolean sortByItemType() {
    List<InventorySlot> previousSlots = new ArrayList<>(slots);
    int oldSelectedSlotIndex = selectedSlotIndex;
    ItemType oldSelectedItem = getSelectedItem();

    slots.sort(
        (first, second) -> {
          if (first.isEmpty()) {
            return second.isEmpty() ? 0 : 1;
          }
          if (second.isEmpty()) {
            return -1;
          }
          return Integer.compare(first.getItemType().ordinal(), second.getItemType().ordinal());
        });

    if (slots.equals(previousSlots)) {
      return false;
    }

    if (oldSelectedItem != null) {
      selectedSlotIndex = findItemSlot(oldSelectedItem);
    }

    notifyInventoryChanged();
    notifySelectionChangedIfNeeded(oldSelectedSlotIndex, oldSelectedItem);
    return true;
  }

  // ---------
  // Internal helpers
  // ---------

  /**
   * Selects the next or previous owned item.
   *
   * @param direction 1 for next, -1 for previous
   * @return selected item, or null when the inventory contains no item
   */
  private ItemType selectRelative(int direction) {
    int oldSelectedSlotIndex = selectedSlotIndex;
    ItemType oldSelectedItem = getSelectedItem();
    int startIndex = selectedSlotIndex;
    if (startIndex < 0) {
      startIndex = direction > 0 ? -1 : 0;
    }

    selectedSlotIndex = findRelativeOccupiedSlot(startIndex, direction);
    notifySelectionChangedIfNeeded(oldSelectedSlotIndex, oldSelectedItem);
    return getSelectedItem();
  }

  private boolean isValidSlotIndex(int index) {
    return index >= 0 && index < slots.size();
  }

  private int findItemSlot(ItemType item) {
    for (int index = 0; index < slots.size(); index++) {
      if (slots.get(index).getItemType() == item) {
        return index;
      }
    }
    return -1;
  }

  private int findEmptySlot() {
    for (int index = 0; index < slots.size(); index++) {
      if (slots.get(index).isEmpty()) {
        return index;
      }
    }
    return -1;
  }

  private int findRelativeOccupiedSlot(int startIndex, int direction) {
    for (int offset = 1; offset <= slots.size(); offset++) {
      int candidateIndex = Math.floorMod(startIndex + direction * offset, slots.size());
      if (!slots.get(candidateIndex).isEmpty()) {
        return candidateIndex;
      }
    }
    return -1;
  }

  private void notifySelectionChangedIfNeeded(int oldSlotIndex, ItemType oldItem) {
    if (oldSlotIndex != selectedSlotIndex || oldItem != getSelectedItem()) {
      notifySelectionChanged();
    }
  }

  // ---------
  // Events
  // ---------

  /** Triggers the inventory changed event after a successful inventory mutation. */
  private void notifyInventoryChanged() {
    // Constructor operations happen before the component is attached to an Entity.
    if (entity != null) {
      entity.getEvents().trigger("inventoryChanged");
    }
  }

  /** Triggers the inventory selection changed event after the active selection changes. */
  private void notifySelectionChanged() {
    if (entity != null) {
      entity.getEvents().trigger("inventorySelectionChanged");
    }
  }
}
