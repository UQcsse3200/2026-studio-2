package com.csse3200.game.components.itemdictionary;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemType;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Tracks which item types the player has discovered.
 *
 * <p>An item is discovered when the player successfully picks it up for the first time.
 */
public class ItemDictionaryComponent extends Component {
  private final Set<ItemType> discoveredItems = EnumSet.noneOf(ItemType.class);

  @Override
  public void create() {
    entity.getEvents().addListener("itemPickedUp", this::onItemPickedUp);
  }

  /**
   * Handles a successful item pickup and unlocks that item in the dictionary.
   *
   * @param item item that was picked up
   */
  private void onItemPickedUp(Item item) {
    if (item == null) {
      return;
    }

    unlockItem(item.getItemType());
  }

  /**
   * Unlocks an item type in the dictionary.
   *
   * @param itemType item type to unlock
   * @return true if the item was newly unlocked
   */
  public boolean unlockItem(ItemType itemType) {
    if (itemType == null) {
      return false;
    }

    boolean newlyUnlocked = discoveredItems.add(itemType);

    if (newlyUnlocked && entity != null) {
      entity.getEvents().trigger("itemDictionaryChanged");
    }

    return newlyUnlocked;
  }

  /**
   * Checks whether an item has been discovered.
   *
   * @param itemType item type to check
   * @return true if the item has been discovered
   */
  public boolean isDiscovered(ItemType itemType) {
    return itemType != null && discoveredItems.contains(itemType);
  }

  /**
   * Returns all discovered item types.
   *
   * @return unmodifiable set of discovered items
   */
  public Set<ItemType> getDiscoveredItems() {
    return Collections.unmodifiableSet(EnumSet.copyOf(discoveredItems));
  }

  /**
   * Returns the number of discovered item types.
   *
   * @return number of discovered items
   */
  public int getDiscoveredCount() {
    return discoveredItems.size();
  }
}
