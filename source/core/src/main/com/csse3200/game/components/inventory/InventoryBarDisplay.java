package com.csse3200.game.components.inventory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/** Displays the player's inventory bar at the bottom of the screen. */
public class InventoryBarDisplay extends UIComponent {
  private Table table;

  @Override
  public void create() {
    super.create();

    entity.getEvents().addListener("inventoryChanged", this::refresh);

    entity.getEvents().addListener("inventorySelectionChanged", this::refresh);

    entity.getEvents().addListener("backpackOpened", this::hideBar);

    entity.getEvents().addListener("backpackClosed", this::showBar);

    addActors();
  }

  private String getItemTexture(ItemType itemType) {
    return switch (itemType) {
      case ARROW -> "images/arrow.png";
      case RopeArrow -> "images/rope_arrow.png";
      default -> "images/heart.png";
    };
  }

  /** Refreshes the inventory bar when the inventory changes. */
  private void refresh() {
    if (table != null) {
      populateSlots();
    }
  }

  /** Hides the quick bar while the backpack is open. */
  private void hideBar() {
    if (table != null) {
      table.setVisible(false);
    }
  }

  /** Displays the quick bar after the backpack is closed. */
  private void showBar() {
    if (table != null) {
      table.setVisible(true);
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

    for (int slotIndex = 0; slotIndex < inventory.getHotbarSlotCount(); slotIndex++) {
      int slotNumber = slotIndex + 1;
      boolean selected = inventory.getSelectedSlotIndex() == slotIndex;
      InventorySlot inventorySlot = inventory.getSlot(slotIndex);
      Table slot;
      if (inventorySlot == null || inventorySlot.isEmpty()) {
        slot = createEmptySlot(slotNumber, selected);
      } else {
        slot =
            createSlot(
                slotNumber, inventorySlot.getItemType(), inventorySlot.getQuantity(), selected);
      }

      table.add(slot).width(160f).height(90f).pad(8f);
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
      case CONSUMABLE -> "Consumable";
      default -> item.toString();
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

    Texture texture =
        ServiceLocator.getResourceService().getAsset(getItemTexture(item), Texture.class);

    Image icon = new Image(texture);

    Label numberLabel = new Label(Integer.toString(slotNumber), skin, "large");

    Label itemLabel = new Label(getItemDisplayName(item), skin);

    Label countLabel = new Label("x" + count, skin);

    slot.add(numberLabel).width(25f).left().padLeft(5f).padRight(5f);

    slot.add(icon).size(36f, 36f).padRight(8f);

    slot.add(itemLabel).expandX().left();

    slot.row();

    slot.add(countLabel).colspan(3).right().padRight(5f).padBottom(3f);

    return slot;
  }

  /**
   * Creates an empty inventory slot.
   *
   * @param slotNumber slot number displayed to the player
   * @return the created empty slot
   */
  private Table createEmptySlot(int slotNumber, boolean selected) {
    Table slot = new Table();
    slot.pad(8f);
    slot.setBackground(skin.getDrawable(selected ? "selection" : "button-c"));

    Label numberLabel = new Label(Integer.toString(slotNumber), skin, "large");

    Label emptyLabel = new Label("EMPTY", skin);

    slot.add(numberLabel).width(25f).left().padLeft(5f).padRight(5f);

    slot.add(emptyLabel).expandX().center();

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
