package com.csse3200.game.components.shop;

import com.csse3200.game.components.item.ItemType;
import java.util.Objects;

/** One item the shop sells, including its gold price and how many copies one purchase gives. */
public final class ShopListing {
  private final ItemType itemType;
  private final int price;
  private final int quantity;

  /**
   * Creates a shop listing.
   *
   * @param itemType item sold by the shop
   * @param price gold cost of one purchase
   * @param quantity number of items granted per purchase
   */
  public ShopListing(ItemType itemType, int price, int quantity) {
    if (itemType == null || price < 0 || quantity <= 0) {
      throw new IllegalArgumentException(
          "Shop listings require an item, a non-negative price, and a positive quantity");
    }
    this.itemType = itemType;
    this.price = price;
    this.quantity = quantity;
  }

  /**
   * @return item sold by this listing
   */
  public ItemType getItemType() {
    return itemType;
  }

  /**
   * @return gold cost of one purchase
   */
  public int getPrice() {
    return price;
  }

  /**
   * @return number of items granted per purchase
   */
  public int getQuantity() {
    return quantity;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ShopListing listing)) {
      return false;
    }
    return price == listing.price && quantity == listing.quantity && itemType == listing.itemType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemType, price, quantity);
  }
}
