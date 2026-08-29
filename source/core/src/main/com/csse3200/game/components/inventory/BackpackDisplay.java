package com.csse3200.game.components.inventory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Displays the player's backpack inventory.
 *
 * <p>The backpack reads its grid dimensions from InventoryComponent. Selecting an occupied slot
 * displays information about that item in the item details panel.
 */
public class BackpackDisplay extends UIComponent {
  private Table table;
  private Table inventoryTable;
  private Table detailsTable;
  private Table contentTable;
  private ItemType selectedItem;

  private boolean visible = false;

  @Override
  public void create() {
    super.create();

    table = new Table();
    table.setFillParent(true);

    // Backpack is hidden by default.
    table.setVisible(false);

    contentTable = new Table();
    inventoryTable = new Table();
    detailsTable = new Table();

    Label title = new Label("Inventory", skin, "large");

    contentTable.add(title).colspan(2).padBottom(20f);

    contentTable.row();

    contentTable.add(inventoryTable).padRight(40f);

    contentTable.add(detailsTable).width(250f).top();

    table.add(contentTable);

    entity.getEvents().addListener("inventoryChanged", this::refresh);

    entity.getEvents().addListener("toggleBackpack", this::toggleBackpack);

    entity.getEvents().addListener("inventorySelectionChanged", this::refresh);

    populateSlots();
    showEmptyDetails();

    stage.addActor(table);
  }

  /** Rebuilds all backpack slots using the current InventoryComponent data. */
  private void populateSlots() {
    inventoryTable.clear();

    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);

    for (int slotIndex = 0; slotIndex < inventory.getSlotCount(); slotIndex++) {
      int slotNumber = slotIndex + 1;
      InventorySlot inventorySlot = inventory.getSlot(slotIndex);
      Table slot;
      if (inventorySlot == null || inventorySlot.isEmpty()) {
        slot = createEmptySlot(slotNumber);
      } else {
        slot =
            createItemSlot(
                slotNumber, slotIndex, inventorySlot.getItemType(), inventorySlot.getQuantity());
      }

      inventoryTable.add(slot).width(95f).height(95f).pad(5f);

      if (slotNumber % inventory.getColumns() == 0) {
        inventoryTable.row();
      }
    }
  }

  /**
   * Creates a backpack slot containing an item.
   *
   * @param itemType item stored in the slot
   * @param quantity quantity of the item
   * @return slot table
   */
  private Table createItemSlot(int slotNumber, int slotIndex, ItemType itemType, int quantity) {

    Table slot = new Table();

    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);

    if (slotIndex == inventory.getSelectedSlotIndex()) {
      slot.setBackground(skin.getDrawable("selection"));
    } else {
      slot.setBackground(skin.getDrawable("button-c"));
    }

    Texture texture =
        ServiceLocator.getResourceService().getAsset(getItemTexture(itemType), Texture.class);

    Image icon = new Image(texture);

    Label itemName = new Label(getItemDisplayName(itemType), skin);

    itemName.setWrap(true);
    itemName.setAlignment(1);

    Label quantityLabel = new Label("x" + quantity, skin);

    Label slotLabel =
        new Label(
            slotNumber <= inventory.getHotbarSlotCount() ? Integer.toString(slotNumber) : "", skin);

    slot.add(slotLabel).width(18f).left().padLeft(2f);

    slot.add(icon).size(40f, 40f).padRight(4f);

    slot.row();

    slot.row();

    slot.add(itemName).colspan(2).width(85f).center();

    slot.row();

    slot.add(quantityLabel).colspan(2).center();

    slot.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {

            selectedItem = itemType;

            showItemDetails(itemType, quantity);

            populateSlots();
          }
        });

    return slot;
  }

  /**
   * Creates an empty backpack slot.
   *
   * @return empty slot table
   */
  private Table createEmptySlot(int slotNumber) {
    Table slot = new Table();
    slot.pad(8f);
    slot.setBackground(skin.getDrawable("button-c"));

    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
    Label slotLabel =
        new Label(
            slotNumber <= inventory.getHotbarSlotCount() ? Integer.toString(slotNumber) : "", skin);

    Label emptyLabel = new Label("EMPTY", skin);

    slot.add(slotLabel).width(18f).left().padLeft(2f);

    slot.add(emptyLabel).expandX().center();

    return slot;
  }

  /**
   * Displays information for the selected item.
   *
   * @param itemType selected item
   * @param quantity current item quantity
   */
  private void showItemDetails(ItemType itemType, int quantity) {

    detailsTable.clear();

    Label title = new Label("Item Details", skin);

    Label name = new Label("Name: " + getItemDisplayName(itemType), skin);

    Label amount = new Label("Quantity: " + quantity, skin);

    Label type = new Label("Type: " + itemType, skin);

    Label description = new Label("Description:\n" + getItemDescription(itemType), skin);

    description.setWrap(true);

    detailsTable.add(title).padBottom(20f);

    detailsTable.row();

    detailsTable.add(name).left().padBottom(10f);

    detailsTable.row();

    detailsTable.add(amount).left().padBottom(10f);

    detailsTable.row();

    detailsTable.add(type).left().padBottom(10f);

    detailsTable.row();

    detailsTable.add(description).width(220f).left();
  }

  /** Displays the default item details panel when no item has been selected. */
  private void showEmptyDetails() {
    detailsTable.clear();

    detailsTable.add(new Label("Item Details", skin)).padBottom(20f);

    detailsTable.row();

    detailsTable.add(new Label("Select an item", skin));
  }

  /**
   * Returns a readable item name for the UI.
   *
   * @param itemType item type
   * @return readable item name
   */
  private String getItemDisplayName(ItemType itemType) {
    return switch (itemType) {
      case ARROW -> "Arrow";
      case RopeArrow -> "Rope Arrow";
      default -> itemType.toString();
    };
  }

  /**
   * Returns a short item description.
   *
   * <p>These descriptions are currently UI placeholders. They can later be obtained directly from
   * the Item classes when the inventory stores full Item objects.
   *
   * @param itemType item type
   * @return item description
   */
  private String getItemDescription(ItemType itemType) {
    return switch (itemType) {
      case ARROW -> "A standard arrow used with the bow.";

      case RopeArrow -> "A special arrow designed for utility and traversal.";

      default -> "No description available.";
    };
  }

  /**
   * Returns the texture path used for an item icon.
   *
   * @param itemType item type
   * @return texture path
   */
  private String getItemTexture(ItemType itemType) {
    return switch (itemType) {
      case ARROW -> "images/arrow.png";
      case RopeArrow -> "images/rope_arrow.png";
      default -> "images/heart.png";
    };
  }

  /** Refreshes the backpack after inventory data changes. */
  private void refresh() {
    populateSlots();

    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);

    ItemType selectedItem = inventory.getSelectedItem();

    if (selectedItem == null) {
      showEmptyDetails();
    } else {
      showItemDetails(selectedItem, inventory.getItemCount(selectedItem));
    }
  }

  /** Displays the backpack and hides the quick bar. */
  public void showBackpack() {
    visible = true;
    table.setVisible(true);

    entity.getEvents().trigger("backpackOpened");
  }

  /** Hides the backpack and restores the quick bar. */
  public void hideBackpack() {
    visible = false;
    table.setVisible(false);

    entity.getEvents().trigger("backpackClosed");
  }

  /** Toggles between showing and hiding the backpack. */
  public void toggleBackpack() {
    if (visible) {
      hideBackpack();
    } else {
      showBackpack();
    }
  }

  /**
   * Returns whether the backpack is currently visible.
   *
   * @return true if the backpack is visible
   */
  public boolean isBackpackVisible() {
    return visible;
  }

  @Override
  public void draw(SpriteBatch batch) {
    // Drawing is handled by the stage.
  }

  @Override
  public void dispose() {
    if (table != null) {
      table.remove();
    }

    super.dispose();
  }
}
