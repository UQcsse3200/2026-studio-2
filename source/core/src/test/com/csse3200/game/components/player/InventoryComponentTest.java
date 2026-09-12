package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.inventory.InventorySlot;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class InventoryComponentTest {
  // ---------
  // Grid dimensions and resizing
  // ---------

  @Test
  void shouldUseDefaultThreeByEightLayout() {
    InventoryComponent inventory = new InventoryComponent(0);

    assertEquals(3, inventory.getRows());
    assertEquals(8, inventory.getColumns());
    assertEquals(8, inventory.getHotbarSlotCount());
    assertEquals(24, inventory.getCapacity());
  }

  @Test
  void shouldUseConfiguredGridDimensions() {
    InventoryComponent inventory = new InventoryComponent(0, 2, 4);

    assertEquals(2, inventory.getRows());
    assertEquals(4, inventory.getColumns());
    assertEquals(4, inventory.getHotbarSlotCount());
    assertEquals(8, inventory.getSlotCount());
  }

  @Test
  void shouldKeepCapacityConstructorAsSingleRowLayout() {
    InventoryComponent inventory = new InventoryComponent(0, 4);

    assertEquals(1, inventory.getRows());
    assertEquals(4, inventory.getColumns());
    assertEquals(4, inventory.getCapacity());
  }

  @Test
  void shouldRejectInvalidGridDimensions() {
    assertThrows(IllegalArgumentException.class, () -> new InventoryComponent(0, 0, 8));
    assertThrows(IllegalArgumentException.class, () -> new InventoryComponent(0, 3, 0));
    assertThrows(IllegalArgumentException.class, () -> new InventoryComponent(0, 3, 10));
    assertThrows(
        IllegalArgumentException.class, () -> new InventoryComponent(0, Integer.MAX_VALUE, 9));
  }

  @Test
  void shouldAddRowWithoutMovingExistingSlots() {
    InventoryComponent inventory = new InventoryComponent(0, 2, 3);
    Entity player = new Entity().addComponent(inventory);
    int[] inventoryEvents = {0};
    int[] selectionEvents = {0};
    player.getEvents().addListener("inventoryChanged", () -> inventoryEvents[0]++);
    player.getEvents().addListener("inventorySelectionChanged", () -> selectionEvents[0]++);
    inventory.addItem(ItemType.ARROW, 1);
    inventory.addItem(ItemType.RopeArrow, 1);
    inventoryEvents[0] = 0;
    selectionEvents[0] = 0;

    assertTrue(inventory.addRow());

    assertEquals(3, inventory.getRows());
    assertEquals(3, inventory.getColumns());
    assertEquals(9, inventory.getCapacity());
    assertEquals(ItemType.ARROW, inventory.getSlot(0).getItemType());
    assertEquals(ItemType.RopeArrow, inventory.getSlot(1).getItemType());
    assertTrue(inventory.isSlotEmpty(6));
    assertTrue(inventory.isSlotEmpty(8));
    assertEquals(1, inventoryEvents[0]);
    assertEquals(0, selectionEvents[0]);
  }

  @Test
  void shouldAddColumnWhileKeepingCoordinatesAndSelection() {
    InventoryComponent inventory = new InventoryComponent(0, 2, 2);
    Entity player = new Entity().addComponent(inventory);
    int[] inventoryEvents = {0};
    int[] selectionEvents = {0};
    player.getEvents().addListener("inventoryChanged", () -> inventoryEvents[0]++);
    player.getEvents().addListener("inventorySelectionChanged", () -> selectionEvents[0]++);
    inventory.addItem(ItemType.ARROW, 1);
    inventory.addItem(ItemType.RopeArrow, 1);
    inventory.addItem(ItemType.CONSUMABLE, 1);
    inventory.selectSlot(2);
    inventoryEvents[0] = 0;
    selectionEvents[0] = 0;

    assertTrue(inventory.addColumn());

    assertEquals(2, inventory.getRows());
    assertEquals(3, inventory.getColumns());
    assertEquals(6, inventory.getCapacity());
    assertEquals(ItemType.ARROW, inventory.getSlot(0).getItemType());
    assertEquals(ItemType.RopeArrow, inventory.getSlot(1).getItemType());
    assertTrue(inventory.isSlotEmpty(2));
    assertEquals(ItemType.CONSUMABLE, inventory.getSlot(3).getItemType());
    assertEquals(3, inventory.getSelectedSlotIndex());
    assertEquals(ItemType.CONSUMABLE, inventory.getSelectedItem());
    assertEquals(1, inventoryEvents[0]);
    assertEquals(1, selectionEvents[0]);
  }

  @Test
  void shouldRejectAddingColumnBeyondNineWithoutEvents() {
    InventoryComponent inventory = new InventoryComponent(0, 2, 9);
    Entity player = new Entity().addComponent(inventory);
    int[] inventoryEvents = {0};
    int[] selectionEvents = {0};
    player.getEvents().addListener("inventoryChanged", () -> inventoryEvents[0]++);
    player.getEvents().addListener("inventorySelectionChanged", () -> selectionEvents[0]++);

    assertFalse(inventory.addColumn());

    assertEquals(9, inventory.getColumns());
    assertEquals(18, inventory.getCapacity());
    assertEquals(0, inventoryEvents[0]);
    assertEquals(0, selectionEvents[0]);
  }

  @Test
  void shouldKeepFirstRowSelectionIndexWhenAddingColumn() {
    InventoryComponent inventory = new InventoryComponent(0, 2, 2);
    Entity player = new Entity().addComponent(inventory);
    int[] selectionEvents = {0};
    player.getEvents().addListener("inventorySelectionChanged", () -> selectionEvents[0]++);
    inventory.selectSlot(1);
    selectionEvents[0] = 0;

    assertTrue(inventory.addColumn());

    assertEquals(1, inventory.getSelectedSlotIndex());
    assertEquals(0, selectionEvents[0]);
  }

  // ---------
  // Slot structure, access, and stacking
  // ---------

  @Test
  void shouldInitialiseConfiguredEmptySlots() {
    InventoryComponent inventory = new InventoryComponent(0, 3);

    assertEquals(3, inventory.getSlotCount());
    assertEquals(3, inventory.getSlots().size());
    assertTrue(inventory.isSlotEmpty(0));
    assertTrue(inventory.isSlotEmpty(1));
    assertTrue(inventory.isSlotEmpty(2));
  }

  @Test
  void shouldPlaceAndStackItemsWithoutMovingSlots() {
    InventoryComponent inventory = new InventoryComponent(0, 3);

    assertTrue(inventory.addItem(ItemType.ARROW, 2));
    assertTrue(inventory.addItem(ItemType.ARROW, 3));
    assertTrue(inventory.addItem(ItemType.RopeArrow, 1));

    assertEquals(ItemType.ARROW, inventory.getSlot(0).getItemType());
    assertEquals(5, inventory.getSlot(0).getQuantity());
    assertEquals(ItemType.RopeArrow, inventory.getSlot(1).getItemType());
    assertTrue(inventory.isSlotEmpty(2));
  }

  @Test
  void shouldClearSlotWhenFinalQuantityIsRemoved() {
    InventoryComponent inventory = new InventoryComponent(0, 2);
    inventory.addItem(ItemType.ARROW, 2);

    assertTrue(inventory.removeItem(ItemType.ARROW, 2));

    assertTrue(inventory.isSlotEmpty(0));
    assertEquals(0, inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldExposeSafeReadOnlySlotState() {
    InventoryComponent inventory = new InventoryComponent(0, 2);

    assertNull(inventory.getSlot(-1));
    assertNull(inventory.getSlot(2));
    assertFalse(inventory.isSlotEmpty(-1));
    assertFalse(inventory.isSlotEmpty(2));
    assertThrows(UnsupportedOperationException.class, () -> inventory.getSlots().clear());
  }

  @Test
  void shouldKeepSlotSnapshotsIndependentFromLaterChanges() {
    InventoryComponent inventory = new InventoryComponent(0, 2);
    List<InventorySlot> snapshot = inventory.getSlots();

    inventory.addItem(ItemType.ARROW, 1);

    assertTrue(snapshot.get(0).isEmpty());
    assertEquals(ItemType.ARROW, inventory.getSlot(0).getItemType());
  }

  @Test
  void shouldRejectStackQuantityOverflowWithoutChangingInventory() {
    InventoryComponent inventory = new InventoryComponent(0, 1);
    inventory.addItem(ItemType.ARROW, Integer.MAX_VALUE);

    assertFalse(inventory.addItem(ItemType.ARROW, 1));
    assertEquals(Integer.MAX_VALUE, inventory.getItemCount(ItemType.ARROW));
  }

  // ---------
  // Slot selection and reordering
  // ---------

  @Test
  void shouldSelectPhysicalSlotIncludingEmptySlot() {
    InventoryComponent inventory = new InventoryComponent(0, 3);
    inventory.addItem(ItemType.ARROW, 1);

    assertTrue(inventory.selectSlot(2));
    assertEquals(2, inventory.getSelectedSlotIndex());
    assertNull(inventory.getSelectedItem());
    assertFalse(inventory.selectSlot(3));
  }

  @Test
  void shouldSwapOccupiedSlots() {
    InventoryComponent inventory = new InventoryComponent(0, 3);
    inventory.addItem(ItemType.ARROW, 2);
    inventory.addItem(ItemType.RopeArrow, 1);

    assertTrue(inventory.swapSlots(0, 1));

    assertEquals(ItemType.RopeArrow, inventory.getSlot(0).getItemType());
    assertEquals(ItemType.ARROW, inventory.getSlot(1).getItemType());
  }

  @Test
  void shouldKeepSelectionAtOriginalIndexWhenSwappingWithEmptySlot() {
    InventoryComponent inventory = new InventoryComponent(0, 3);
    Entity player = new Entity().addComponent(inventory);
    int[] inventoryEvents = {0};
    int[] selectionEvents = {0};
    player.getEvents().addListener("inventoryChanged", () -> inventoryEvents[0]++);
    player.getEvents().addListener("inventorySelectionChanged", () -> selectionEvents[0]++);
    inventory.addItem(ItemType.ARROW, 1);
    inventoryEvents[0] = 0;
    selectionEvents[0] = 0;

    assertTrue(inventory.swapSlots(0, 2));

    assertEquals(0, inventory.getSelectedSlotIndex());
    assertTrue(inventory.isSlotEmpty(0));
    assertEquals(ItemType.ARROW, inventory.getSlot(2).getItemType());
    assertNull(inventory.getSelectedItem());
    assertEquals(1, inventoryEvents[0]);
    assertEquals(1, selectionEvents[0]);
  }

  @Test
  void shouldNotifyBothEventsWhenSelectedOccupiedSlotIsSwapped() {
    InventoryComponent inventory = new InventoryComponent(0, 2);
    Entity player = new Entity().addComponent(inventory);
    int[] inventoryEvents = {0};
    int[] selectionEvents = {0};
    player.getEvents().addListener("inventoryChanged", () -> inventoryEvents[0]++);
    player.getEvents().addListener("inventorySelectionChanged", () -> selectionEvents[0]++);
    inventory.addItem(ItemType.ARROW, 1);
    inventory.addItem(ItemType.RopeArrow, 1);
    inventoryEvents[0] = 0;
    selectionEvents[0] = 0;

    assertTrue(inventory.swapSlots(0, 1));

    assertEquals(0, inventory.getSelectedSlotIndex());
    assertEquals(ItemType.RopeArrow, inventory.getSelectedItem());
    assertEquals(1, inventoryEvents[0]);
    assertEquals(1, selectionEvents[0]);
  }

  @Test
  void shouldNotNotifySelectionWhenUnselectedSlotsAreSwapped() {
    InventoryComponent inventory = new InventoryComponent(0, 3);
    Entity player = new Entity().addComponent(inventory);
    int[] inventoryEvents = {0};
    int[] selectionEvents = {0};
    player.getEvents().addListener("inventoryChanged", () -> inventoryEvents[0]++);
    player.getEvents().addListener("inventorySelectionChanged", () -> selectionEvents[0]++);
    inventory.addItem(ItemType.ARROW, 1);
    inventory.addItem(ItemType.RopeArrow, 1);
    inventory.addItem(ItemType.CONSUMABLE, 1);
    inventoryEvents[0] = 0;
    selectionEvents[0] = 0;

    assertTrue(inventory.swapSlots(1, 2));

    assertEquals(0, inventory.getSelectedSlotIndex());
    assertEquals(ItemType.ARROW, inventory.getSelectedItem());
    assertEquals(1, inventoryEvents[0]);
    assertEquals(0, selectionEvents[0]);
  }

  @Test
  void shouldRejectInvalidAndNoOpSwapsWithoutEvents() {
    InventoryComponent inventory = new InventoryComponent(0, 2);
    Entity player = new Entity().addComponent(inventory);
    int[] inventoryEvents = {0};
    int[] selectionEvents = {0};
    player.getEvents().addListener("inventoryChanged", () -> inventoryEvents[0]++);
    player.getEvents().addListener("inventorySelectionChanged", () -> selectionEvents[0]++);

    assertFalse(inventory.swapSlots(-1, 0));
    assertFalse(inventory.swapSlots(0, 2));
    assertFalse(inventory.swapSlots(0, 0));
    assertFalse(inventory.swapSlots(0, 1));
    assertEquals(0, inventoryEvents[0]);
    assertEquals(0, selectionEvents[0]);
  }

  @Test
  void shouldNotifyWhenAnItemEntersTheSelectedEmptySlot() {
    InventoryComponent inventory = new InventoryComponent(0, 2);
    Entity player = new Entity().addComponent(inventory);
    int[] selectionEvents = {0};
    player.getEvents().addListener("inventorySelectionChanged", () -> selectionEvents[0]++);
    inventory.selectSlot(0);
    selectionEvents[0] = 0;

    assertTrue(inventory.addItem(ItemType.ARROW, 1));

    assertEquals(ItemType.ARROW, inventory.getSelectedItem());
    assertEquals(1, selectionEvents[0]);
  }

  @Test
  void shouldCycleThroughItemsInPhysicalSlotOrder() {
    InventoryComponent inventory = new InventoryComponent(0, 3);
    inventory.addItem(ItemType.ARROW, 1);
    inventory.addItem(ItemType.RopeArrow, 1);
    inventory.addItem(ItemType.CONSUMABLE, 1);
    inventory.swapSlots(0, 2);

    assertEquals(ItemType.CONSUMABLE, inventory.getSelectedItem());
    assertEquals(ItemType.RopeArrow, inventory.selectNext());
    assertEquals(ItemType.ARROW, inventory.selectNext());
    assertEquals(ItemType.RopeArrow, inventory.selectPrevious());
  }

  @Test
  void shouldSortOccupiedSlotsByNumericItemId() {
    InventoryComponent inventory = new InventoryComponent(0, 5);
    inventory.addItem(ItemType.ColdArrow, 5);
    inventory.addItem(ItemType.CONSUMABLE, 3);
    inventory.addItem(ItemType.FireArrow, 4);
    inventory.addItem(ItemType.RopeArrow, 2);
    inventory.addItem(ItemType.ARROW, 1);

    assertTrue(inventory.sortByItemId());

    assertEquals(ItemType.ARROW, inventory.getSlot(0).getItemType());
    assertEquals(ItemType.RopeArrow, inventory.getSlot(1).getItemType());
    assertEquals(ItemType.CONSUMABLE, inventory.getSlot(2).getItemType());
    assertEquals(ItemType.FireArrow, inventory.getSlot(3).getItemType());
    assertEquals(ItemType.ColdArrow, inventory.getSlot(4).getItemType());
  }

  @Test
  void shouldSortOccupiedSlotsByItemIdAndMoveEmptySlotsToEnd() {
    InventoryComponent inventory = new InventoryComponent(0, 5);
    inventory.addItem(ItemType.CONSUMABLE, 3);
    inventory.addItem(ItemType.RopeArrow, 2);
    inventory.addItem(ItemType.ARROW, 1);

    assertTrue(inventory.sortByItemId());

    assertEquals(ItemType.ARROW, inventory.getSlot(0).getItemType());
    assertEquals(1, inventory.getSlot(0).getQuantity());
    assertEquals(ItemType.RopeArrow, inventory.getSlot(1).getItemType());
    assertEquals(2, inventory.getSlot(1).getQuantity());
    assertEquals(ItemType.CONSUMABLE, inventory.getSlot(2).getItemType());
    assertEquals(3, inventory.getSlot(2).getQuantity());
    assertTrue(inventory.isSlotEmpty(3));
    assertTrue(inventory.isSlotEmpty(4));
  }

  @Test
  void shouldKeepSelectedItemAndNotifyWhenSortingMovesIt() {
    InventoryComponent inventory = new InventoryComponent(0, 3);
    Entity player = new Entity().addComponent(inventory);
    int[] inventoryEvents = {0};
    int[] selectionEvents = {0};
    player.getEvents().addListener("inventoryChanged", () -> inventoryEvents[0]++);
    player.getEvents().addListener("inventorySelectionChanged", () -> selectionEvents[0]++);
    inventory.addItem(ItemType.CONSUMABLE, 1);
    inventory.addItem(ItemType.ARROW, 1);
    inventoryEvents[0] = 0;
    selectionEvents[0] = 0;

    assertTrue(inventory.sortByItemId());

    assertEquals(ItemType.CONSUMABLE, inventory.getSelectedItem());
    assertEquals(1, inventory.getSelectedSlotIndex());
    assertEquals(1, inventoryEvents[0]);
    assertEquals(1, selectionEvents[0]);
  }

  @Test
  void shouldNotNotifyWhenInventoryIsAlreadySorted() {
    InventoryComponent inventory = new InventoryComponent(0, 3);
    Entity player = new Entity().addComponent(inventory);
    int[] inventoryEvents = {0};
    int[] selectionEvents = {0};
    player.getEvents().addListener("inventoryChanged", () -> inventoryEvents[0]++);
    player.getEvents().addListener("inventorySelectionChanged", () -> selectionEvents[0]++);
    inventory.addItem(ItemType.ARROW, 1);
    inventory.addItem(ItemType.RopeArrow, 1);
    inventoryEvents[0] = 0;
    selectionEvents[0] = 0;

    assertFalse(inventory.sortByItemId());

    assertEquals(0, inventoryEvents[0]);
    assertEquals(0, selectionEvents[0]);
  }

  @Test
  void shouldMapQuickSlotEventToPhysicalSlot() {
    InventoryComponent inventory = new InventoryComponent(0, 8);
    Entity player = new Entity().addComponent(inventory);
    player.create();
    inventory.addItem(ItemType.ARROW, 1);

    player.getEvents().trigger("selectQuickSlot", 5);

    assertEquals(5, inventory.getSelectedSlotIndex());
    assertNull(inventory.getSelectedItem());
  }

  @Test
  void shouldIgnoreNumberKeyOutsideDefaultHotbar() {
    InventoryComponent inventory = new InventoryComponent(0);
    Entity player = new Entity().addComponent(inventory);
    player.create();
    inventory.selectSlot(0);

    player.getEvents().trigger("selectQuickSlot", 8);

    assertEquals(0, inventory.getSelectedSlotIndex());
  }

  @Test
  void shouldIgnoreNumberKeyOutsideNarrowHotbar() {
    InventoryComponent inventory = new InventoryComponent(0, 2, 4);
    Entity player = new Entity().addComponent(inventory);
    player.create();
    inventory.selectSlot(0);

    player.getEvents().trigger("selectQuickSlot", 4);

    assertEquals(0, inventory.getSelectedSlotIndex());
  }

  @Test
  void shouldAllowNinthNumberKeyAfterAddingNinthColumn() {
    InventoryComponent inventory = new InventoryComponent(0, 2, 8);
    Entity player = new Entity().addComponent(inventory);
    player.create();
    inventory.addColumn();

    player.getEvents().trigger("selectQuickSlot", 8);

    assertEquals(8, inventory.getSelectedSlotIndex());
  }

  // ---------
  // Gold
  // ---------

  @Test
  void shouldSetGetGold() {
    InventoryComponent inventory = new InventoryComponent(100);
    assertEquals(100, inventory.getGold());

    inventory.setGold(150);
    assertEquals(150, inventory.getGold());

    inventory.setGold(-50);
    assertEquals(0, inventory.getGold());
  }

  @Test
  void shouldCheckHasGold() {
    InventoryComponent inventory = new InventoryComponent(150);
    assertTrue(inventory.hasGold(100));
    assertFalse(inventory.hasGold(200));
  }

  @Test
  void shouldAddGold() {
    InventoryComponent inventory = new InventoryComponent(100);
    inventory.addGold(-500);
    assertEquals(0, inventory.getGold());

    inventory.addGold(100);
    inventory.addGold(-20);
    assertEquals(80, inventory.getGold());
  }

  // ---------
  // Existing inventory behaviour
  // ---------

  @Test
  void shouldAddItemToInventory() {
    InventoryComponent inventory = new InventoryComponent(100);

    assertTrue(inventory.addItem(ItemType.ARROW, 3));

    assertTrue(inventory.hasItem(ItemType.ARROW));
    assertEquals(3, inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldStackSameItemType() {
    InventoryComponent inventory = new InventoryComponent(100);

    assertTrue(inventory.addItem(ItemType.ARROW, 2));
    assertTrue(inventory.addItem(ItemType.ARROW, 3));

    assertEquals(5, inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldRejectInvalidItemQuantities() {
    InventoryComponent inventory = new InventoryComponent(100);

    assertFalse(inventory.addItem(ItemType.ARROW, 0));
    assertFalse(inventory.addItem(ItemType.ARROW, -1));
    assertFalse(inventory.addItem(null, 1));

    assertEquals(0, inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldRejectNewItemTypeWhenInventoryIsFull() {
    InventoryComponent inventory = new InventoryComponent(100, 1);

    assertTrue(inventory.addItem(ItemType.ARROW, 1));
    assertTrue(inventory.isFull());

    assertFalse(inventory.addItem(ItemType.RopeArrow, 1));
    assertEquals(0, inventory.getItemCount(ItemType.RopeArrow));
  }

  @Test
  void shouldAllowStackingWhenInventoryIsFull() {
    InventoryComponent inventory = new InventoryComponent(100, 1);

    assertTrue(inventory.addItem(ItemType.ARROW, 1));
    assertTrue(inventory.isFull());

    assertTrue(inventory.addItem(ItemType.ARROW, 2));
    assertEquals(3, inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldRemoveItemQuantity() {
    InventoryComponent inventory = new InventoryComponent(100);
    inventory.addItem(ItemType.ARROW, 5);

    assertTrue(inventory.removeItem(ItemType.ARROW, 2));

    assertEquals(3, inventory.getItemCount(ItemType.ARROW));
    assertTrue(inventory.hasItem(ItemType.ARROW));
  }

  @Test
  void shouldNotRemoveMoreItemsThanStored() {
    InventoryComponent inventory = new InventoryComponent(100);
    inventory.addItem(ItemType.ARROW, 2);

    assertFalse(inventory.removeItem(ItemType.ARROW, 3));

    assertEquals(2, inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldSelectFirstAddedItem() {
    InventoryComponent inventory = new InventoryComponent(100);

    assertNull(inventory.getSelectedItem());

    inventory.addItem(ItemType.ARROW, 1);

    assertEquals(ItemType.ARROW, inventory.getSelectedItem());
  }

  @Test
  void shouldCycleThroughOwnedItems() {
    InventoryComponent inventory = new InventoryComponent(100);
    inventory.addItem(ItemType.ARROW, 1);
    inventory.addItem(ItemType.RopeArrow, 1);

    assertEquals(ItemType.ARROW, inventory.getSelectedItem());

    assertEquals(ItemType.RopeArrow, inventory.selectNext());
    assertEquals(ItemType.ARROW, inventory.selectNext());
    assertEquals(ItemType.RopeArrow, inventory.selectPrevious());
  }

  @Test
  void shouldClearSelectionWhenLastItemIsRemoved() {
    InventoryComponent inventory = new InventoryComponent(100);
    inventory.addItem(ItemType.ARROW, 1);

    assertTrue(inventory.removeItem(ItemType.ARROW, 1));

    assertFalse(inventory.hasItem(ItemType.ARROW));
    assertNull(inventory.getSelectedItem());
  }

  @Test
  void shouldSelectRemainingItemWhenSelectedItemIsRemoved() {
    InventoryComponent inventory = new InventoryComponent(100);
    inventory.addItem(ItemType.ARROW, 1);
    inventory.addItem(ItemType.RopeArrow, 1);

    assertTrue(inventory.removeItem(ItemType.ARROW, 1));

    assertEquals(ItemType.RopeArrow, inventory.getSelectedItem());
  }

  @Test
  void shouldRejectInvalidCapacity() {
    assertThrows(IllegalArgumentException.class, () -> new InventoryComponent(100, 0));

    assertThrows(IllegalArgumentException.class, () -> new InventoryComponent(100, -1));
    assertThrows(IllegalArgumentException.class, () -> new InventoryComponent(100, 10));
  }

  // ---------
  // Events
  // ---------

  @Test
  void shouldTriggerInventoryChangedEvent() {
    InventoryComponent inventory = new InventoryComponent(100);
    Entity player = new Entity().addComponent(inventory);
    int[] eventCount = {0};

    player.getEvents().addListener("inventoryChanged", () -> eventCount[0]++);

    inventory.addItem(ItemType.ARROW, 1);
    assertEquals(1, eventCount[0]);

    inventory.removeItem(ItemType.ARROW, 1);
    assertEquals(2, eventCount[0]);
  }

  @Test
  void shouldTriggerSelectionChangedEvent() {
    InventoryComponent inventory = new InventoryComponent(100);
    Entity player = new Entity().addComponent(inventory);
    int[] eventCount = {0};

    player.getEvents().addListener("inventorySelectionChanged", () -> eventCount[0]++);

    inventory.addItem(ItemType.ARROW, 1);
    assertEquals(1, eventCount[0]);

    inventory.addItem(ItemType.RopeArrow, 1);
    assertEquals(1, eventCount[0]);

    inventory.selectNext();
    assertEquals(2, eventCount[0]);
  }
}
