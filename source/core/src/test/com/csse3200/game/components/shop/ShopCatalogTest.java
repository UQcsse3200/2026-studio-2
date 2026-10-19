package com.csse3200.game.components.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.components.item.ItemType;
import org.junit.jupiter.api.Test;

class ShopCatalogTest {
  @Test
  void listingsShouldHaveValidPricesAndQuantities() {
    assertFalse(ShopCatalog.getListings().isEmpty());
    for (ShopListing listing : ShopCatalog.getListings()) {
      assertNotNull(ShopCatalog.getListing(listing.itemType()));
      assertTrue(listing.price() >= 0);
      assertTrue(listing.quantity() > 0);
    }
  }

  @Test
  void shouldUsePlaceholderPrices() {
    assertEquals(10, ShopCatalog.getListing(ItemType.STANDARD_ARROW).price());
    assertEquals(15, ShopCatalog.getListing(ItemType.FIRE_ARROW).price());
    assertEquals(15, ShopCatalog.getListing(ItemType.ICE_ARROW).price());
    assertEquals(8, ShopCatalog.getListing(ItemType.HEALTH_POTION).price());
    assertEquals(12, ShopCatalog.getListing(ItemType.SPEED_POTION).price());
    assertEquals(12, ShopCatalog.getListing(ItemType.POISON_POTION).price());
  }

  @Test
  void shouldSellEveryItemExceptRopeArrowAndMeleeWeapons() {
    for (ItemType type : ItemType.values()) {
      if (type == ItemType.ROPE_ARROW) {
        assertNull(ShopCatalog.getListing(type));
      } else {
        assertNotNull(ShopCatalog.getListing(type));
      }
    }
  }

  @Test
  void shouldRejectInvalidListings() {
    assertThrows(IllegalArgumentException.class, () -> new ShopListing(null, 1, 1));
    assertThrows(
        IllegalArgumentException.class, () -> new ShopListing(ItemType.STANDARD_ARROW, -1, 1));
    assertThrows(
        IllegalArgumentException.class, () -> new ShopListing(ItemType.STANDARD_ARROW, 1, 0));
  }
}
