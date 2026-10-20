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
  private final int gold;
  private final List<InventorySlot> inventory;
  private final Set<ItemType> discoveredItems;

  private PlayerSnapshot(
      int health, int gold, List<InventorySlot> inventory, Set<ItemType> discoveredItems) {
    this.health = health;
    this.gold = gold;
    this.inventory = inventory;
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

    return new PlayerSnapshot(
        stats.getHealth(), inventory.getGold(), inventory.getSlots(), discovered);
  }

  /**
   * Apply the stored PlayerSnapshot to the given player entity. Updating the gold, health,
   * inventory and item dictionary components
   *
   * @param player - the player to apply the saved PlayerSnapshot to
   */
  public void applyTo(Entity player) {
    player.getComponent(CombatStatsComponent.class).setHealth(health);
    player.getComponent(InventoryComponent.class).setGold(gold);
    player.getComponent(InventoryComponent.class).setSlots(inventory);

    ItemDictionaryComponent dictionary = player.getComponent(ItemDictionaryComponent.class);
    if (dictionary != null) {
      for (ItemType type : discoveredItems) {
        dictionary.unlockItem(type);
      }
    }
  }
}
