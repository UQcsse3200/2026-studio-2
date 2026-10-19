package com.csse3200.game.components.shop;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
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
  void shouldPurchaseSelectedItemThroughBuyButtonAndRefreshOnceBeforeDrawWhilePaused() {
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
    Table details = field(display, "detailsTable", Table.class);
    Actor original = listings.getChildren().first();

    assertNull(findBuyButton(details), "nothing is selected yet, so there is nothing to buy");
    ((Button) original).setChecked(true);
    TextButton buy = findBuyButton(details);
    assertNotNull(buy);
    assertTrue(labels(details).contains(ItemType.STANDARD_ARROW.getDisplayName()));
    assertTrue(labels(details).contains("Price: " + ShopCatalog.STANDARD_ARROW_PRICE + "g"));
    assertFalse(buy.isDisabled());

    buy.fire(new ChangeEvent());
    assertEquals(5, inventory.getItemCount(ItemType.STANDARD_ARROW));
    assertEquals(40, inventory.getGold());
    assertSame(original, listings.getChildren().first());
    display.draw(batch);
    assertTrue(labels(field(display, "table", Table.class)).contains("Gold: 40"));
    assertTrue(labels(field(display, "table", Table.class)).contains("Purchased Standard Arrow."));
    Actor refreshed = listings.getChildren().first();
    assertTrue(((Button) refreshed).isChecked(), "the selection survives a refresh");
    assertNotNull(findBuyButton(details));
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
    Table details = field(display, "detailsTable", Table.class);
    ((Button) field(display, "listingsTable", Table.class).getChildren().first()).setChecked(true);
    assertTrue(findBuyButton(details).isDisabled());
    assertEquals(ShopComponent.PurchaseResult.INSUFFICIENT_GOLD, shop.buy(ItemType.STANDARD_ARROW));
    display.draw(batch);
    assertTrue(labels(field(display, "table", Table.class)).contains("Not enough gold."));
    assertEquals(0, inventory.getGold());
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
    player.getEvents().trigger("closeShop");
    player.dispose();
    assertEquals(0, stage.getActors().size);
  }

  @Test
  void shouldClearTheDetailsWhenTheSelectedItemIsClickedAgain() {
    InventoryComponent inventory = new InventoryComponent(50);
    ShopDisplay display = new ShopDisplay();
    register(
        new Entity()
            .addComponent(inventory)
            .addComponent(new ShopComponent())
            .addComponent(display));
    display.open();
    Table details = field(display, "detailsTable", Table.class);
    Button item = (Button) field(display, "listingsTable", Table.class).getChildren().first();

    item.setChecked(true);
    assertNotNull(findBuyButton(details));
    item.setChecked(false);

    assertNull(findBuyButton(details));
  }

  /** Finds the buy button inside the details panel, or null if no item is selected. */
  private static TextButton findBuyButton(Group root) {
    for (Actor child : root.getChildren()) {
      if (child instanceof TextButton button && "Buy".contentEquals(button.getText())) {
        return button;
      }
      if (child instanceof Group group) {
        TextButton found = findBuyButton(group);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
