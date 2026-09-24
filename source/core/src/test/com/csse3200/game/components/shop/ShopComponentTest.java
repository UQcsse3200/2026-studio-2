package com.csse3200.game.components.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    ShopListing listing = ShopCatalog.getListing(ItemType.STANDARD_ARROW);

    boolean[] purchased = {false};
    player.getEvents().addListener("itemPurchased", (ItemType type) -> purchased[0] = true);

    assertEquals(PurchaseResult.SUCCESS, shop.buy(ItemType.STANDARD_ARROW));
    assertTrue(purchased[0]);
    assertEquals(50 - listing.getPrice(), inventory.getGold());
    assertEquals(listing.getQuantity(), inventory.getItemCount(ItemType.STANDARD_ARROW));
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

    assertTrue(shop.canBuy(arrows));
  }

  Entity createPlayer(int gold) {
    Entity player =
        new Entity().addComponent(new InventoryComponent(gold)).addComponent(new ShopComponent());
    player.create();
    return player;
  }
}
