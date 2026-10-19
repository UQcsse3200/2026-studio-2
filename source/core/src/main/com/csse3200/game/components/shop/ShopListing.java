package com.csse3200.game.components.shop;

import com.csse3200.game.components.item.ItemType;

/**
 * One item the shop sells, including its gold price and how many copies one purchase gives.
 *
 * @param itemType item sold by the shop
 * @param price gold cost of one purchase
 * @param quantity number of items granted per purchase
 */
public record ShopListing(ItemType itemType, int price, int quantity) {
  /** Validates a shop listing. */
  public ShopListing {
    if (itemType == null || price < 0 || quantity <= 0) {
      throw new IllegalArgumentException(
          "Shop listings require an item, a non-negative price, and a positive quantity");
    }
  }
}
