package com.csse3200.game.components.player;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.inventory.InventorySlot;
import com.csse3200.game.entities.Entity;
import java.util.List;

public class PlayerSnapshot {
  private final int health;
  private final int gold;
  private final List<InventorySlot> inventory;

  private PlayerSnapshot(int health, int gold, List<InventorySlot> inventory) {
    this.health = health;
    this.gold = gold;
    this.inventory = inventory;
  }

  /**
   * Capture and store the components of the player that are needed between levels (eg. health,
   * gold, inventory items)
   *
   * @param player - the player to take a snapshot of
   * @return a PlayerSnapshot component storing the gold, health and inventory of the player
   */
  public static PlayerSnapshot capture(Entity player) {
    CombatStatsComponent stats = player.getComponent(CombatStatsComponent.class);
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);

    return new PlayerSnapshot(stats.getHealth(), inventory.getGold(), inventory.getSlots());
  }

  /**
   * Apply the stored PlayerSnapshot to the given player entity. Updating the gold, health and
   * inventory components
   *
   * @param player - the player to apply the saved PlayerSnapshot to
   */
  public void applyTo(Entity player) {
    player.getComponent(CombatStatsComponent.class).setHealth(health);
    player.getComponent(InventoryComponent.class).setGold(gold);
    player.getComponent(InventoryComponent.class).setSlots(inventory);
  }
}
