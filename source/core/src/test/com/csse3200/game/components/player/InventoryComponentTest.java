package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class InventoryComponentTest {
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
  }

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
