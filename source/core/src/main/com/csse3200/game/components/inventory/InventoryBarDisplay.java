package com.csse3200.game.components.inventory;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.ui.UIComponent;

/** Displays the player's inventory bar at the bottom of the screen. */
public class InventoryBarDisplay extends UIComponent {
  private Table table;

  @Override
  public void create() {
    super.create();

    entity.getEvents().addListener("inventoryChanged", this::refresh);
    entity.getEvents().addListener("inventorySelectionChanged", this::refresh);

    addActors();
  }

  /** Refreshes the inventory bar when the inventory changes. */
  private void refresh() {
    if (table != null) {
      populateSlots();
    }
  }

  /** Creates and positions the inventory bar. */
  private void addActors() {
    table = new Table();
    table.bottom();
    table.setFillParent(true);
    table.padBottom(20f);

    populateSlots();

    stage.addActor(table);
  }

  /** Populates the inventory bar with occupied and empty slots. */
  private void populateSlots() {
    table.clearChildren();

    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);

    int slotNumber = 1;

    for (ItemType item : ItemType.values()) {
      int count = inventory.getItemCount(item);

      if (count > 0) {
        Table slot = createSlot(slotNumber, item, count, inventory.getSelectedItem() == item);

        table.add(slot).width(160f).height(90f).pad(8f);
        slotNumber++;
      }
    }

    while (slotNumber <= inventory.getCapacity()) {
      Table emptySlot = createEmptySlot(slotNumber);
      table.add(emptySlot).width(160f).height(90f).pad(8f);
      slotNumber++;
    }
  }

  /**
   * Returns a user-friendly display name for an item type.
   *
   * @param item item type
   * @return display name
   */
  private String getItemDisplayName(ItemType item) {
    return switch (item) {
      case ARROW -> "Arrow";
      case RopeArrow -> "Rope Arrow";
    };
  }

  /**
   * Creates one inventory slot.
   *
   * @param slotNumber slot number displayed to the player
   * @param item item stored in the slot
   * @param count quantity of the item
   * @param selected whether this item is currently selected
   * @return the created slot table
   */
  private Table createSlot(int slotNumber, ItemType item, int count, boolean selected) {

    Table slot = new Table();
    slot.pad(8f);

    if (selected) {
      slot.setBackground(skin.getDrawable("selection"));
    } else {
      slot.setBackground(skin.getDrawable("button-c"));
    }

    Label numberLabel = new Label(Integer.toString(slotNumber), skin, "large");

    Label itemLabel = new Label(getItemDisplayName(item), skin);

    Label countLabel = new Label("x" + count, skin);

    slot.add(numberLabel).padRight(10f);
    slot.add(itemLabel).expandX().left();
    slot.row();

    slot.add(countLabel).colspan(2).padTop(5f).right();

    return slot;
  }

  /**
   * Creates an empty inventory slot.
   *
   * @param slotNumber slot number displayed to the player
   * @return the created empty slot
   */
  private Table createEmptySlot(int slotNumber) {
    Table slot = new Table();
    slot.pad(8f);
    slot.setBackground(skin.getDrawable("button-c"));

    Label numberLabel = new Label(Integer.toString(slotNumber), skin, "large");

    Label emptyLabel = new Label("EMPTY", skin);

    slot.add(numberLabel).padRight(10f);
    slot.add(emptyLabel);

    return slot;
  }

  @Override
  public void draw(SpriteBatch batch) {
    // Drawing is handled by the stage.
  }

  @Override
  public void dispose() {
    super.dispose();

    if (table != null) {
      table.remove();
    }
  }
}
