package com.csse3200.game.components.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.components.item.ItemType;
import org.junit.jupiter.api.Test;

class ShopCatalogTest {
  @Test
  void listingsShouldHaveValidPricesAndQuantities() {
    assertFalse(ShopCatalog.getListings().isEmpty());
    for (ShopListing listing : ShopCatalog.getListings()) {
      assertNotNull(ShopCatalog.getListing(listing.getItemType()));
      assertTrue(listing.getPrice() >= 0);
      assertTrue(listing.getQuantity() > 0);
    }
  }

  @Test
  void shouldUsePlaceholderPrices() {
    assertEquals(10, ShopCatalog.getListing(ItemType.ARROW).getPrice());
    assertEquals(20, ShopCatalog.getListing(ItemType.RopeArrow).getPrice());
    assertEquals(15, ShopCatalog.getListing(ItemType.FireArrow).getPrice());
    assertEquals(15, ShopCatalog.getListing(ItemType.ColdArrow).getPrice());
    assertEquals(8, ShopCatalog.getListing(ItemType.CONSUMABLE).getPrice());
  }

  @Test
  void shouldRejectInvalidListings() {
    assertThrows(IllegalArgumentException.class, () -> new ShopListing(null, 1, 1));
    assertThrows(IllegalArgumentException.class, () -> new ShopListing(ItemType.ARROW, -1, 1));
    assertThrows(IllegalArgumentException.class, () -> new ShopListing(ItemType.ARROW, 1, 0));
  }
}
