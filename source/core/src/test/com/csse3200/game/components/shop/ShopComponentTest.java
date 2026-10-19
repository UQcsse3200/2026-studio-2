package com.csse3200.game.components.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.shop.ShopComponent.PurchaseResult;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(GameExtension.class)
class ShopComponentTest {
  @Test
  void shouldBuyItemAndDeductGold() {
    Entity player = createPlayer(50);
    ShopComponent shop = player.getComponent(ShopComponent.class);
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    ShopListing listing = ShopCatalog.getListing(ItemType.STANDARD_ARROW);

    boolean[] purchased = {false};
    player.getEvents().addListener("itemPurchased", (ItemType type) -> purchased[0] = true);

    assertEquals(PurchaseResult.SUCCESS, shop.buy(ItemType.STANDARD_ARROW));
    assertTrue(purchased[0]);
    assertEquals(50 - listing.price(), inventory.getGold());
    assertEquals(listing.quantity(), inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldRejectPurchaseWhenGoldIsInsufficient() {
    Entity player = createPlayer(0);
    ShopComponent shop = player.getComponent(ShopComponent.class);
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);

    boolean[] failed = {false};
    player.getEvents().addListener("purchaseFailed", (String reason) -> failed[0] = true);

    assertEquals(PurchaseResult.INSUFFICIENT_GOLD, shop.buy(ItemType.STANDARD_ARROW));
    assertTrue(failed[0]);
    assertEquals(0, inventory.getGold());
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldRejectPurchaseWhenInventoryIsFull() {
    Entity player =
        new Entity().addComponent(new InventoryComponent(50, 1)).addComponent(new ShopComponent());
    player.create();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.HEALTH_POTION, 1);

    ShopComponent shop = player.getComponent(ShopComponent.class);
    assertEquals(PurchaseResult.INVENTORY_FULL, shop.buy(ItemType.STANDARD_ARROW));
    assertEquals(50, inventory.getGold());
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldRejectRopeArrow() {
    Entity player = createPlayer(50);
    ShopComponent shop = player.getComponent(ShopComponent.class);

    assertEquals(PurchaseResult.INVALID, shop.buy(ItemType.ROPE_ARROW));
  }

  @Test
  void shouldRejectUnknownListing() {
    Entity player = createPlayer(50);
    ShopComponent shop = player.getComponent(ShopComponent.class);

    assertEquals(PurchaseResult.INVALID, shop.buy((ShopListing) null));
    assertEquals(PurchaseResult.INVALID, shop.buy((ItemType) null));
  }

  @Test
  void canBuyRequiresGoldAndInventorySpace() {
    Entity player = createPlayer(ShopCatalog.STANDARD_ARROW_PRICE);
    ShopComponent shop = player.getComponent(ShopComponent.class);
    ShopListing arrows = ShopCatalog.getListing(ItemType.STANDARD_ARROW);
    ShopListing fireArrows = ShopCatalog.getListing(ItemType.FIRE_ARROW);

    assertTrue(shop.canBuy(arrows));
    assertFalse(shop.canAfford(fireArrows));
    assertFalse(shop.canBuy(fireArrows));
  }

  Entity createPlayer(int gold) {
    Entity player =
        new Entity().addComponent(new InventoryComponent(gold)).addComponent(new ShopComponent());
    player.create();
    return player;
  }

  @Test
  void rejectsEligibilityChecksWithoutInventoryOrValidListing() {
    ShopComponent shop = new ShopComponent();
    new Entity().addComponent(shop);
    ShopListing arrows = ShopCatalog.getListing(ItemType.STANDARD_ARROW);
    assertFalse(shop.canAfford(arrows));
    assertFalse(shop.canStore(arrows));
    assertFalse(shop.canBuy(arrows));

    ShopComponent stockedShop = createPlayer(50).getComponent(ShopComponent.class);
    assertFalse(stockedShop.canAfford(null));
    assertFalse(stockedShop.canStore(null));
    ShopListing unavailable = new ShopListing(ItemType.ROPE_ARROW, 1, 1);
    assertFalse(stockedShop.canAfford(unavailable));
    assertFalse(stockedShop.canStore(unavailable));
  }

  @ParameterizedTest
  @CsvSource({"1, true", "2147483647, false"})
  void existingStacksRespectQuantityOverflow(int quantity, boolean canStore) {
    Entity player = createPlayer(50);
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    assertTrue(inventory.addItem(ItemType.STANDARD_ARROW, quantity));
    ShopComponent shop = player.getComponent(ShopComponent.class);
    assertEquals(canStore, shop.canStore(ShopCatalog.getListing(ItemType.STANDARD_ARROW)));
    assertEquals(quantity, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }
}
