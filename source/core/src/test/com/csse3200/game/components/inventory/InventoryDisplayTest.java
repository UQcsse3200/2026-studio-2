package com.csse3200.game.components.inventory;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.itemdictionary.ItemDictionaryComponent;
import com.csse3200.game.components.itemdictionary.ItemDictionaryDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.ui.UiTestSupport;
import org.junit.jupiter.api.Test;

class InventoryDisplayTest extends UiTestSupport {
  @Test
  void shouldSwapStacksWhenDraggingOntoAnotherSlot() {
    InventoryComponent inventory = new InventoryComponent(0);
    inventory.addItem(ItemType.FIRE_ARROW, 3);
    inventory.addItem(ItemType.ICE_ARROW, 2);
    BackpackDisplay backpack = new BackpackDisplay();
    register(new Entity().addComponent(inventory).addComponent(backpack));
    backpack.showBackpack();
    field(backpack, "table", Table.class).validate();
    Table grid = field(backpack, "inventoryTable", Table.class);
    grid.validate();
    Actor source = grid.getChildren().get(0);
    Actor target = grid.getChildren().get(1);
    int actorCount = stage.getActors().size;

    dragTo(source, target.localToStageCoordinates(new Vector2(45, 45)));
    backpack.draw(batch);

    assertEquals(ItemType.ICE_ARROW, inventory.getSlot(0).getItemType());
    assertEquals(2, inventory.getSlot(0).getQuantity());
    assertEquals(ItemType.FIRE_ARROW, inventory.getSlot(1).getItemType());
    assertEquals(3, inventory.getSlot(1).getQuantity());
    assertEquals(actorCount, stage.getActors().size);
  }

  @Test
  void shouldCancelDragOutsideSlotsWithoutMovingOrLosingItems() {
    InventoryComponent inventory = new InventoryComponent(0);
    inventory.addItem(ItemType.FIRE_ARROW, 3);
    BackpackDisplay backpack = new BackpackDisplay();
    register(new Entity().addComponent(inventory).addComponent(backpack));
    backpack.showBackpack();
    field(backpack, "table", Table.class).validate();
    Table grid = field(backpack, "inventoryTable", Table.class);
    grid.validate();
    Actor source = grid.getChildren().first();
    int actorCount = stage.getActors().size;

    dragTo(source, new Vector2(-10, -10));
    backpack.draw(batch);

    assertEquals(ItemType.FIRE_ARROW, inventory.getSlot(0).getItemType());
    assertEquals(3, inventory.getSlot(0).getQuantity());
    assertEquals(1f, source.getColor().a);
    assertEquals(actorCount, stage.getActors().size);
  }

  private void dragTo(Actor source, Vector2 target) {
    DragListener drag = null;
    for (var listener : source.getListeners()) {
      if (listener instanceof DragListener dragListener) {
        drag = dragListener;
      }
    }
    assertNotNull(drag);
    InputEvent event = new InputEvent();
    event.setStage(stage);
    event.setTarget(source);
    Vector2 start = source.localToStageCoordinates(new Vector2(45, 45));
    event.setStageX(start.x);
    event.setStageY(start.y);
    drag.dragStart(event, 45, 45, 0);
    event.setStageX(target.x);
    event.setStageY(target.y);
    drag.drag(event, 0, 0, 0);
    drag.dragStop(event, 0, 0, 0);
  }

  @Test
  void shouldHighlightSelectedEmptyBackpackSlot() {
    InventoryComponent inventory = new InventoryComponent(0);
    BackpackDisplay backpack = new BackpackDisplay();
    register(new Entity().addComponent(inventory).addComponent(backpack));
    inventory.addItem(ItemType.STANDARD_ARROW, 2);
    backpack.showBackpack();
    backpack.draw(batch);
    Table grid = field(backpack, "inventoryTable", Table.class);
    Object selected = ((Table) grid.getChildren().get(0)).getBackground();
    click(grid.getChildren().get(1));
    backpack.draw(batch);
    assertEquals(1, inventory.getSelectedSlotIndex());
    assertSame(selected, ((Table) grid.getChildren().get(1)).getBackground());
    assertTrue(labels(field(backpack, "detailsTable", Table.class)).contains("Select an item"));
  }

  @Test
  void shouldDeferHiddenBackpackRefreshAndCombineEventsBeforeDrawing() {
    InventoryComponent inventory = new InventoryComponent(0);
    BackpackDisplay backpack = new BackpackDisplay();
    register(new Entity().addComponent(inventory).addComponent(backpack));
    Table grid = field(backpack, "inventoryTable", Table.class);
    Actor original = grid.getChildren().first();
    inventory.addItem(ItemType.STANDARD_ARROW, 2);
    inventory.addItem(ItemType.STANDARD_ARROW, 3);
    backpack.draw(batch);
    assertSame(original, grid.getChildren().first());
    backpack.showBackpack();
    backpack.draw(batch);
    assertNotSame(original, grid.getChildren().first());
    assertTrue(labels(field(backpack, "detailsTable", Table.class)).contains("Quantity: 5"));
    Actor refreshed = grid.getChildren().first();
    backpack.draw(batch);
    assertSame(refreshed, grid.getChildren().first());
  }

  @Test
  void shouldCombineHotbarChangesUntilDrawing() {
    InventoryComponent inventory = new InventoryComponent(0);
    InventoryBarDisplay bar = new InventoryBarDisplay();
    register(new Entity().addComponent(inventory).addComponent(bar));
    Table slots = field(bar, "table", Table.class);
    Actor original = slots.getChildren().first();
    inventory.addItem(ItemType.FIRE_ARROW, 2);
    inventory.addItem(ItemType.FIRE_ARROW, 3);
    assertSame(original, slots.getChildren().first());
    bar.draw(batch);
    assertTrue(labels(slots).contains("x5"));
    Actor refreshed = slots.getChildren().first();
    bar.draw(batch);
    assertSame(refreshed, slots.getChildren().first());
  }

  @Test
  void shouldKeepPanelsMutuallyExclusiveAndRestoreHotbar() {
    BackpackDisplay backpack = new BackpackDisplay();
    InventoryBarDisplay bar = new InventoryBarDisplay();
    ItemDictionaryDisplay dictionary = new ItemDictionaryDisplay();
    register(
        new Entity()
            .addComponent(new InventoryComponent(0))
            .addComponent(bar)
            .addComponent(backpack)
            .addComponent(new ItemDictionaryComponent())
            .addComponent(dictionary));
    backpack.showBackpack();
    assertFalse(field(bar, "table", Table.class).isVisible());
    dictionary.showDictionary();
    assertFalse(backpack.isBackpackVisible());
    assertTrue(dictionary.isDictionaryVisible());
    assertFalse(field(bar, "table", Table.class).isVisible());
    backpack.showBackpack();
    assertFalse(dictionary.isDictionaryVisible());
    backpack.hideBackpack();
    assertTrue(field(bar, "table", Table.class).isVisible());
  }

  @Test
  void shouldReleaseOnlyTheDisposedDisplaysSlotTextures() {
    BackpackDisplay backpack = new BackpackDisplay();
    InventoryBarDisplay bar = new InventoryBarDisplay();
    Entity backpackOwner =
        register(new Entity().addComponent(new InventoryComponent(0)).addComponent(backpack));
    register(new Entity().addComponent(new InventoryComponent(0)).addComponent(bar));
    Table grid = field(backpack, "inventoryTable", Table.class);
    Texture backpackTexture =
        ((NinePatchDrawable) ((Table) grid.getChildren().first()).getBackground())
            .getPatch()
            .getTexture();
    Table hotbar = field(bar, "table", Table.class);
    Texture hotbarTexture =
        ((NinePatchDrawable) ((Table) hotbar.getChildren().first()).getBackground())
            .getPatch()
            .getTexture();
    backpackOwner.dispose();
    assertEquals(0, backpackTexture.getTextureObjectHandle());
    assertNotEquals(0, hotbarTexture.getTextureObjectHandle());
    assertNull(field(backpack, "table", Table.class).getStage());
  }
}
