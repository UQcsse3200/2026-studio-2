package com.csse3200.game.components.inventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/** Displays the player's inventory bar at the bottom of the screen as drawn rounded boxes. */
public class InventoryBarDisplay extends UIComponent {

  private static final float SLOT_WIDTH = 90f;
  private static final float SLOT_HEIGHT = 90f;
  private static final float SLOT_SPACING = 10f;
  private static final float ICON_SIZE = 56f;

  private Table root;
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
    return itemType.getTexturePath();
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
    root = new Table();
    root.bottom();
    root.setFillParent(true);
    root.padBottom(20f);

    table = new Table();
    table.defaults().space(SLOT_SPACING);
    populateSlots();

    root.add(table);

    stage.addActor(root);
  }

  /** Populates the inventory bar with occupied and empty slots. */
  private void populateSlots() {
    table.clearChildren();

    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);

    for (int slotIndex = 0; slotIndex < inventory.getHotbarSlotCount(); slotIndex++) {
      boolean selected = inventory.getSelectedSlotIndex() == slotIndex;
      InventorySlot inventorySlot = inventory.getSlot(slotIndex);
      Table slot;
      if (inventorySlot == null || inventorySlot.isEmpty()) {
        slot = createEmptySlot(selected);
      } else {
        slot = createSlot(inventorySlot.getItemType(), inventorySlot.getQuantity(), selected);
      }

      table.add(slot).width(SLOT_WIDTH).height(SLOT_HEIGHT);
    }
  }

  /**
   * Creates one occupied inventory slot.
   *
   * @param item item stored in the slot
   * @param count quantity of the item
   * @param selected whether this item is currently selected
   * @return the created slot table
   */
  private Table createSlot(ItemType item, int count, boolean selected) {
    Table slot = new Table();
    slot.setBackground(
        selected ? InventorySlotStyle.getSelectedBox() : InventorySlotStyle.getNormalBox());
    slot.pad(6f);

    Texture texture =
        ServiceLocator.getResourceService().getAsset(getItemTexture(item), Texture.class);
    Image icon = new Image(texture);

    Label countLabel =
        new Label("x" + count, new Label.LabelStyle(skin.getFont("font"), Color.WHITE));
    countLabel.setColor(Color.WHITE);

    slot.add(icon).size(ICON_SIZE, ICON_SIZE).expand().center();
    slot.row();
    slot.add(countLabel).right().padRight(4f).padBottom(2f);

    return slot;
  }

  /**
   * Creates an empty inventory slot.
   *
   * @param selected whether this slot is currently selected
   * @return the created empty slot
   */
  private Table createEmptySlot(boolean selected) {
    Table slot = new Table();
    slot.setBackground(
        selected ? InventorySlotStyle.getSelectedBox() : InventorySlotStyle.getNormalBox());
    slot.pad(6f);

    slot.add().expand().fill();

    return slot;
  }

  @Override
  public void draw(SpriteBatch batch) {
    // Drawing is handled by the stage.
  }

  @Override
  public void dispose() {
    super.dispose();

    if (root != null) {
      root.remove();
    }
  }
}
