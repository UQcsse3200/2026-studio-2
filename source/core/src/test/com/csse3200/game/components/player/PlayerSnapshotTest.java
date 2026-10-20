package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.*;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.inventory.InventorySlot;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.itemdictionary.ItemDictionaryComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

class PlayerSnapshotTest {
  @Test
  void restoresCapturedHealthGoldAndPhysicalSlotsAfterSourceChanges() {
    Entity source = player(64, 125);
    InventoryComponent inventory = source.getComponent(InventoryComponent.class);
    assertTrue(inventory.addItem(ItemType.STANDARD_ARROW, 12));
    assertTrue(inventory.addItem(ItemType.HEALTH_POTION, 3));
    assertTrue(inventory.swapSlots(1, 10));
    PlayerSnapshot snapshot = PlayerSnapshot.capture(source);
    source.getComponent(CombatStatsComponent.class).setHealth(8);
    inventory.setGold(0);
    inventory.removeItem(ItemType.STANDARD_ARROW, 12);
    inventory.addItem(ItemType.FIRE_ARROW, 7);

    Entity restored = player(100, 999);
    restored.getComponent(InventoryComponent.class).addItem(ItemType.ICE_ARROW, 2);
    snapshot.applyTo(restored);

    InventoryComponent result = restored.getComponent(InventoryComponent.class);
    assertEquals(64, restored.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(125, result.getGold());
    assertEquals(24, result.getSlotCount());
    assertEquals(ItemType.STANDARD_ARROW, result.getSlot(0).getItemType());
    assertEquals(12, result.getSlot(0).getQuantity());
    assertTrue(result.isSlotEmpty(1));
    assertEquals(ItemType.HEALTH_POTION, result.getSlot(10).getItemType());
    assertEquals(3, result.getSlot(10).getQuantity());
    assertFalse(result.hasItem(ItemType.FIRE_ARROW));
    assertFalse(result.hasItem(ItemType.ICE_ARROW));
  }

  @Test
  void restoredInventoryRemainsMutableAndDoesNotChangeSnapshotOrOtherPlayers() {
    Entity source = player(55, 80);
    InventoryComponent original = source.getComponent(InventoryComponent.class);
    original.addItem(ItemType.STANDARD_ARROW, 5);
    original.addItem(ItemType.HEALTH_POTION, 2);
    PlayerSnapshot snapshot = PlayerSnapshot.capture(source);
    Entity first = player(100, 0);
    snapshot.applyTo(first);
    InventoryComponent restored = first.getComponent(InventoryComponent.class);
    assertTrue(restored.removeItem(ItemType.STANDARD_ARROW, 2));
    assertTrue(restored.addItem(ItemType.FIRE_ARROW, 4));
    assertTrue(restored.selectSlot(1));
    assertEquals(ItemType.HEALTH_POTION, restored.getSelectedItem());
    assertTrue(restored.removeItem(ItemType.HEALTH_POTION, 2));
    assertEquals(ItemType.FIRE_ARROW, restored.getSelectedItem());
    assertTrue(restored.addRow());
    assertTrue(restored.addColumn());
    assertEquals(36, restored.getCapacity());
    assertEquals(3, restored.getItemCount(ItemType.STANDARD_ARROW));
    assertEquals(5, original.getItemCount(ItemType.STANDARD_ARROW));
    assertEquals(2, original.getItemCount(ItemType.HEALTH_POTION));
    assertFalse(original.hasItem(ItemType.FIRE_ARROW));
    Entity second = player(100, 0);
    snapshot.applyTo(second);
    InventoryComponent secondInventory = second.getComponent(InventoryComponent.class);
    assertEquals(5, secondInventory.getItemCount(ItemType.STANDARD_ARROW));
    assertEquals(2, secondInventory.getItemCount(ItemType.HEALTH_POTION));
    assertFalse(secondInventory.hasItem(ItemType.FIRE_ARROW));
    assertEquals(24, secondInventory.getCapacity());
  }

  @Test
  void restoresZeroHealthZeroGoldAndEmptyInventoryOverExistingItems() {
    PlayerSnapshot snapshot = PlayerSnapshot.capture(player(0, 0));
    Entity restored = player(100, 500);
    InventoryComponent inventory = restored.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.ROPE_ARROW, 1);
    snapshot.applyTo(restored);
    assertEquals(0, restored.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(0, inventory.getGold());
    assertTrue(inventory.getSlots().stream().allMatch(InventorySlot::isEmpty));
    assertTrue(inventory.addItem(ItemType.STANDARD_ARROW, 1));
    assertTrue(inventory.removeItem(ItemType.STANDARD_ARROW, 1));
  }

  @Test
  void restoresDiscoveredItemsInTheItemDictionary() {
    Entity source = player(64, 125);
    ItemDictionaryComponent dictionary = new ItemDictionaryComponent();
    source.addComponent(dictionary);
    dictionary.unlockItem(ItemType.FIRE_ARROW);
    PlayerSnapshot snapshot = PlayerSnapshot.capture(source);

    Entity restored = player(100, 0);
    ItemDictionaryComponent restoredDictionary = new ItemDictionaryComponent();
    restored.addComponent(restoredDictionary);
    snapshot.applyTo(restored);

    assertTrue(restoredDictionary.isDiscovered(ItemType.FIRE_ARROW));
    assertEquals(1, restoredDictionary.getDiscoveredCount());
  }

  private Entity player(int health, int gold) {
    return new Entity()
        .addComponent(new CombatStatsComponent(health, 100, 10))
        .addComponent(new InventoryComponent(gold));
  }
}
