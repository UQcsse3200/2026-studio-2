package com.csse3200.game.components.shop;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.ui.UiTestSupport;
import org.junit.jupiter.api.Test;

class ShopDisplayTest extends UiTestSupport {
  @Test
  void shouldPurchaseThroughButtonAndRefreshOnceBeforeDrawWhilePaused() {
    InventoryComponent inventory = new InventoryComponent(50);
    ShopDisplay display = new ShopDisplay();
    register(
        new Entity()
            .addComponent(inventory)
            .addComponent(new ShopComponent())
            .addComponent(display));
    display.open();
    assertTrue(entities.getPaused());
    Table listings = field(display, "listingsTable", Table.class);
    Actor original = listings.getChildren().first();
    Table arrowRow = (Table) original;
    TextButton buy = (TextButton) arrowRow.getChildren().peek();
    assertFalse(buy.isDisabled());
    buy.fire(new ChangeEvent());
    assertEquals(5, inventory.getItemCount(ItemType.STANDARD_ARROW));
    assertEquals(40, inventory.getGold());
    assertSame(original, listings.getChildren().first());
    display.draw(batch);
    assertTrue(labels(field(display, "table", Table.class)).contains("Gold: 40"));
    assertTrue(labels(field(display, "table", Table.class)).contains("Purchased Standard Arrow."));
    Actor refreshed = listings.getChildren().first();
    display.draw(batch);
    assertSame(refreshed, listings.getChildren().first());
    display.close();
    assertFalse(entities.getPaused());
    assertFalse(display.isOpen());
    assertFalse(field(display, "table", Table.class).isVisible());
  }

  @Test
  void shouldDisableUnaffordablePurchaseAndShowFailureWithoutChangingInventory() {
    InventoryComponent inventory = new InventoryComponent(0);
    ShopComponent shop = new ShopComponent();
    ShopDisplay display = new ShopDisplay();
    Entity player =
        register(new Entity().addComponent(inventory).addComponent(shop).addComponent(display));
    player.getEvents().trigger("openShop");
    Table row = (Table) field(display, "listingsTable", Table.class).getChildren().first();
    assertTrue(((TextButton) row.getChildren().peek()).isDisabled());
    assertEquals(ShopComponent.PurchaseResult.INSUFFICIENT_GOLD, shop.buy(ItemType.STANDARD_ARROW));
    display.draw(batch);
    assertTrue(labels(field(display, "table", Table.class)).contains("Not enough gold."));
    assertEquals(0, inventory.getGold());
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
    player.getEvents().trigger("closeShop");
    player.dispose();
    assertEquals(0, stage.getActors().size);
  }
}
