package com.csse3200.game.components.shop;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles buying items from the shop catalog using the player's inventory gold.
 *
 * <p>Requires an InventoryComponent on the same entity.
 */
public class ShopComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(ShopComponent.class);

  /** Outcome of an attempted purchase. */
  public enum PurchaseResult {
    SUCCESS,
    INSUFFICIENT_GOLD,
    INVENTORY_FULL,
    ALREADY_OWNED,
    INVALID
  }

  /**
   * Buys the catalog listing for the given item type.
   *
   * @param itemType item to buy
   * @return purchase result
   */
  public PurchaseResult buy(ItemType itemType) {
    return buy(ShopCatalog.getListing(itemType));
  }

  /**
   * Buys a shop listing at the catalog price. Client-supplied prices are ignored.
   *
   * @param listing listing to buy
   * @return purchase result
   */
  public PurchaseResult buy(ShopListing listing) {
    ShopListing offered = listing == null ? null : ShopCatalog.getListing(listing.getItemType());
    if (offered == null) {
      notifyFailed("Cannot buy this item.");
      return PurchaseResult.INVALID;
    }

    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
    if (inventory == null) {
      notifyFailed("Cannot buy this item.");
      return PurchaseResult.INVALID;
    }

    if (!inventory.hasGold(offered.getPrice())) {
      notifyFailed("Not enough gold.");
      return PurchaseResult.INSUFFICIENT_GOLD;
    }

    if (offered.getItemType() == ItemType.RopeArrow && inventory.hasItem(ItemType.RopeArrow)) {
      notifyFailed("You already own this item.");
      return PurchaseResult.ALREADY_OWNED;
    }

    if (!inventory.addItem(offered.getItemType(), offered.getQuantity())) {
      notifyFailed("Inventory is full.");
      return PurchaseResult.INVENTORY_FULL;
    }

    inventory.addGold(-offered.getPrice());
    entity.getEvents().trigger("itemPurchased", offered.getItemType());
    logger.info(
        "Purchased {} x{} for {} gold",
        offered.getItemType().getDisplayName(),
        offered.getQuantity(),
        offered.getPrice());
    return PurchaseResult.SUCCESS;
  }

  /**
   * @param listing listing to check
   * @return true if the player can currently complete this purchase
   */
  public boolean canBuy(ShopListing listing) {
    return canAfford(listing) && canStore(listing);
  }

  /**
   * @param listing listing to check
   * @return true if the player has enough gold
   */
  public boolean canAfford(ShopListing listing) {
    ShopListing offered = offeredListing(listing);
    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
    return offered != null && inventory != null && inventory.hasGold(offered.getPrice());
  }

  /**
   * @param listing listing to check
   * @return true if the inventory can accept this purchase
   */
  public boolean canStore(ShopListing listing) {
    ShopListing offered = offeredListing(listing);
    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
    if (offered == null || inventory == null) {
      return false;
    }

    ItemType itemType = offered.getItemType();
    if (itemType == ItemType.RopeArrow && inventory.hasItem(ItemType.RopeArrow)) {
      return false;
    }

    int currentQuantity = inventory.getItemCount(itemType);
    if (currentQuantity > 0) {
      return (long) currentQuantity + offered.getQuantity() <= Integer.MAX_VALUE;
    }
    return !inventory.isFull();
  }

  private ShopListing offeredListing(ShopListing listing) {
    return listing == null ? null : ShopCatalog.getListing(listing.getItemType());
  }

  private void notifyFailed(String reason) {
    if (entity != null) {
      entity.getEvents().trigger("purchaseFailed", reason);
    }
  }
}
