package com.csse3200.game.files;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.inventory.InventorySlot;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single save slot's contents, serialised to JSON by {@link SaveManager} through {@link
 * FileLoader}. Follows the same convention as {@link UserSettings.Settings}: public fields and an
 * implicit constructor so libGDX can use it A null {@link #player} means the save was created by
 * starting a new game and no checkpoint has been reached yet so the level's own defaults should be
 * used on load.
 */
public class SaveFile {
  private static final Logger logger = LoggerFactory.getLogger(SaveFile.class);

  // Slot this save occupies (0..SaveManager.SLOT_COUNT - 1)
  public int slotIndex;

  // Real-world time of the last write, shown on the save-select screen.
  public long lastModifiedEpochMs;

  // True once the player has won the game (level 3 win condition)
  public boolean completed;

  // Level the save resumes in: "level1", "level2", "level3" or "boss"
  public String currentLevel = "level1";

  // Player stats at the last checkpoint, or null for a brand-new game
  public PlayerSaveData player;

  // Checkpoints activated in {@link #currentLevel}, so respawn and lit torches match
  public List<CheckpointSave> activatedCheckpoints = new ArrayList<>();

  // Player state captured at a checkpoint
  public static class PlayerSaveData {
    public int health;
    public int maxHealth;
    public int gold;

    // Total inventory positions when captured, so expanded inventories keep their size
    public int slotCount;

    // Only occupied slots; empty positions are rebuilt from the default inventory
    public List<ItemSave> inventory = new ArrayList<>();

    /**
     * Records health, max health, gold and inventory from the given player.
     *
     * @param player player entity to capture from
     * @return captured player data, or null if the player has no saveable components
     */
    public static PlayerSaveData capture(Entity player) {
      if (player == null) {
        return null;
      }
      CombatStatsComponent stats = player.getComponent(CombatStatsComponent.class);
      InventoryComponent inventoryComponent = player.getComponent(InventoryComponent.class);
      if (stats == null || inventoryComponent == null) {
        return null;
      }

      PlayerSaveData data = new PlayerSaveData();
      data.health = Math.max(1, stats.getHealth());
      data.maxHealth = stats.getMaxHealth();
      data.gold = inventoryComponent.getGold();

      List<InventorySlot> slots = inventoryComponent.getSlots();
      data.slotCount = slots.size();
      for (int index = 0; index < slots.size(); index++) {
        InventorySlot slot = slots.get(index);
        if (!slot.isEmpty()) {
          data.inventory.add(new ItemSave(index, slot.getItemType().name(), slot.getQuantity()));
        }
      }
      return data;
    }

    /**
     * Applies this data to the given player: maximum health first (so the saved health can clamp
     * correctly), then gold and inventory.
     *
     * @param player player entity to update
     */
    public void applyTo(Entity player) {
      CombatStatsComponent stats = player.getComponent(CombatStatsComponent.class);
      if (stats != null) {
        // addMaxHealth also heals by the same amount; setHealth afterwards fixes the exact value.
        stats.addMaxHealth(maxHealth - stats.getMaxHealth());
        stats.setHealth(health);
      }

      InventoryComponent inventoryComponent = player.getComponent(InventoryComponent.class);
      if (inventoryComponent == null) {
        return;
      }
      inventoryComponent.setGold(gold);
      if (slotCount <= 0) {
        return;
      }
      List<InventorySlot> restored = new ArrayList<>(slotCount);
      for (int index = 0; index < slotCount; index++) {
        restored.add(InventorySlot.empty());
      }
      for (ItemSave item : inventory) {
        ItemType type = SaveFile.resolveItemType(item);
        if (type != null && item.index >= 0 && item.index < slotCount) {
          restored.set(item.index, InventorySlot.of(type, item.quantity));
        }
      }
      inventoryComponent.setSlots(restored);
    }
  }

  /** One occupied inventory position. */
  public static class ItemSave {
    public int index;
    public String itemType;
    public int quantity;

    public ItemSave() {}

    public ItemSave(int index, String itemType, int quantity) {
      this.index = index;
      this.itemType = itemType;
      this.quantity = quantity;
    }
  }

  /** One activated checkpoint, stored in tile coordinates like {@link GridPoint2}. */
  public static class CheckpointSave {
    public int x;
    public int y;

    public CheckpointSave() {}

    public CheckpointSave(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public GridPoint2 toGridPoint2() {
      return new GridPoint2(x, y);
    }
  }

  /**
   * Resolves {@link ItemSave#itemType} to an {@link ItemType}, logging and skipping unknown names
   * (e.g. items removed in a later version of the game).
   *
   * @param item saved inventory entry
   * @return the item type, or null when it cannot be resolved
   */
  public static ItemType resolveItemType(ItemSave item) {
    if (item == null || item.itemType == null) {
      return null;
    }
    try {
      return ItemType.valueOf(item.itemType);
    } catch (IllegalArgumentException e) {
      logger.warn("Skipping unknown saved item type {}", item.itemType);
      return null;
    }
  }
}
