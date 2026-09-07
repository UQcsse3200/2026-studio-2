package com.csse3200.game.components.shop;

import com.csse3200.game.components.item.ItemType;
import java.util.List;

/**
 * Items offered by the shop and their placeholder prices.
 *
 * <p>Starting player gold is 50. These prices are easy to retune in one place.
 */
public final class ShopCatalog {
  public static final int STANDARD_ARROW_PRICE = 10;
  public static final int ROPE_ARROW_PRICE = 20;
  public static final int FIRE_ARROW_PRICE = 15;
  public static final int COLD_ARROW_PRICE = 15;
  public static final int HEALTH_POTION_PRICE = 8;

  public static final int STANDARD_ARROW_QUANTITY = 5;
  public static final int ROPE_ARROW_QUANTITY = 1;
  public static final int FIRE_ARROW_QUANTITY = 5;
  public static final int COLD_ARROW_QUANTITY = 5;
  public static final int HEALTH_POTION_QUANTITY = 1;

  private static final List<ShopListing> LISTINGS =
      List.of(
          new ShopListing(ItemType.ARROW, STANDARD_ARROW_PRICE, STANDARD_ARROW_QUANTITY),
          new ShopListing(ItemType.RopeArrow, ROPE_ARROW_PRICE, ROPE_ARROW_QUANTITY),
          new ShopListing(ItemType.FireArrow, FIRE_ARROW_PRICE, FIRE_ARROW_QUANTITY),
          new ShopListing(ItemType.ColdArrow, COLD_ARROW_PRICE, COLD_ARROW_QUANTITY),
          new ShopListing(ItemType.CONSUMABLE, HEALTH_POTION_PRICE, HEALTH_POTION_QUANTITY));

  /**
   * @return unmodifiable list of items the shop currently sells
   */
  public static List<ShopListing> getListings() {
    return LISTINGS;
  }

  /**
   * Finds the catalog listing for an item type.
   *
   * @param itemType item to look up
   * @return listing, or null if the shop does not sell that item
   */
  public static ShopListing getListing(ItemType itemType) {
    if (itemType == null) {
      return null;
    }
    for (ShopListing listing : LISTINGS) {
      if (listing.getItemType() == itemType) {
        return listing;
      }
    }
    return null;
  }

  private ShopCatalog() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
