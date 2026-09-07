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

@ExtendWith(GameExtension.class)
class ShopComponentTest {
  @Test
  void shouldBuyItemAndDeductGold() {
    Entity player = createPlayer(50);
    ShopComponent shop = player.getComponent(ShopComponent.class);
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    ShopListing listing = ShopCatalog.getListing(ItemType.ARROW);

    boolean[] purchased = {false};
    player.getEvents().addListener("itemPurchased", (ItemType type) -> purchased[0] = true);

    assertEquals(PurchaseResult.SUCCESS, shop.buy(ItemType.ARROW));
    assertTrue(purchased[0]);
    assertEquals(50 - listing.getPrice(), inventory.getGold());
    assertEquals(listing.getQuantity(), inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldRejectPurchaseWhenGoldIsInsufficient() {
    Entity player = createPlayer(0);
    ShopComponent shop = player.getComponent(ShopComponent.class);
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);

    boolean[] failed = {false};
    player.getEvents().addListener("purchaseFailed", (String reason) -> failed[0] = true);

    assertEquals(PurchaseResult.INSUFFICIENT_GOLD, shop.buy(ItemType.ARROW));
    assertTrue(failed[0]);
    assertEquals(0, inventory.getGold());
    assertEquals(0, inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldRejectPurchaseWhenInventoryIsFull() {
    Entity player =
        new Entity()
            .addComponent(new InventoryComponent(50, 1))
            .addComponent(new ShopComponent());
    player.create();
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.CONSUMABLE, 1);

    ShopComponent shop = player.getComponent(ShopComponent.class);
    assertEquals(PurchaseResult.INVENTORY_FULL, shop.buy(ItemType.ARROW));
    assertEquals(50, inventory.getGold());
    assertEquals(0, inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldRejectDuplicateRopeArrow() {
    Entity player = createPlayer(50);
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);
    inventory.addItem(ItemType.RopeArrow, 1);

    ShopComponent shop = player.getComponent(ShopComponent.class);
    assertEquals(PurchaseResult.ALREADY_OWNED, shop.buy(ItemType.RopeArrow));
    assertEquals(50, inventory.getGold());
    assertEquals(1, inventory.getItemCount(ItemType.RopeArrow));
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
    ShopListing arrows = ShopCatalog.getListing(ItemType.ARROW);
    ShopListing ropeArrow = ShopCatalog.getListing(ItemType.RopeArrow);

    assertTrue(shop.canBuy(arrows));
    assertFalse(shop.canAfford(ropeArrow));
    assertFalse(shop.canBuy(ropeArrow));
  }

  Entity createPlayer(int gold) {
    Entity player =
        new Entity().addComponent(new InventoryComponent(gold)).addComponent(new ShopComponent());
    player.create();
    return player;
  }
}
