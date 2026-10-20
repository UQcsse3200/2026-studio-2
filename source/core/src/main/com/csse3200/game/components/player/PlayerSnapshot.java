package com.csse3200.game.components.player;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.inventory.InventorySlot;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.itemdictionary.ItemDictionaryComponent;
import com.csse3200.game.entities.Entity;
import java.util.List;
import java.util.Set;

public class PlayerSnapshot {
  private final int health;
  private final int maxHealth;
  private final int gold;
  private final List<InventorySlot> inventory;
  private final int rows;
  private final int columns;
  private final int selectedSlot;
  private final Set<ItemType> discoveredItems;

  private PlayerSnapshot(
      int health, int maxHealth, InventoryComponent inventory, Set<ItemType> discoveredItems) {
    this.health = health;
    this.maxHealth = maxHealth;
    this.gold = inventory.getGold();
    this.inventory = inventory.getSlots();
    this.rows = inventory.getRows();
    this.columns = inventory.getColumns();
    this.selectedSlot = inventory.getSelectedSlotIndex();
    this.discoveredItems = discoveredItems;
  }

  /**
   * Capture and store the components of the player that are needed between levels (eg. health,
   * gold, inventory items, discovered items)
   *
   * @param player - the player to take a snapshot of
   * @return a PlayerSnapshot component storing the gold, health, inventory and discovered items of
   *     the player
   */
  public static PlayerSnapshot capture(Entity player) {
    CombatStatsComponent stats = player.getComponent(CombatStatsComponent.class);
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    ItemDictionaryComponent dictionary = player.getComponent(ItemDictionaryComponent.class);
    Set<ItemType> discovered = dictionary == null ? Set.of() : dictionary.getDiscoveredItems();

    return new PlayerSnapshot(stats.getHealth(), stats.getMaxHealth(), inventory, discovered);
  }

  /** Returns the saved player's gold without consuming the snapshot. */
  public int getGold() {
    return gold;
  }

  /** Returns a snapshot with updated gold, preserving the saved health and inventory. */
  public PlayerSnapshot withGold(int updatedGold) {
    if (updatedGold < 0) {
      throw new IllegalArgumentException("Gold cannot be negative");
    }
    return new PlayerSnapshot(health, updatedGold, inventory);
  }

  /**
   * Apply the stored PlayerSnapshot to the given player entity. Updating the gold, health,
   * inventory and item dictionary components
   *
   * @param player - the player to apply the saved PlayerSnapshot to
   */
  public void applyTo(Entity player) {
    CombatStatsComponent stats = player.getComponent(CombatStatsComponent.class);
    stats.addMaxHealth(maxHealth - stats.getMaxHealth());
    stats.setHealth(health);
    InventoryComponent targetInventory = player.getComponent(InventoryComponent.class);
    targetInventory.setGold(gold);
    while (targetInventory.getRows() < rows) {
      targetInventory.addRow();
    }
    while (targetInventory.getColumns() < columns) {
      targetInventory.addColumn();
    }
    targetInventory.setSlots(inventory);
    targetInventory.selectSlot(selectedSlot);

    ItemDictionaryComponent dictionary = player.getComponent(ItemDictionaryComponent.class);
    if (dictionary != null) {
      for (ItemType type : discoveredItems) {
        dictionary.unlockItem(type);
      }
    }
  }
}
