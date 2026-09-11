package com.csse3200.game.components.inventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/** Displays the player's inventory bar at the bottom of the screen. */
public class InventoryBarDisplay extends UIComponent {

  private static final String INVENTORY_BACKGROUND_TEXTURE = "images/Inventory_background.png";

  /** Native pixel size of the background art. */
  private static final float BG_WIDTH = 853f;

  private static final float BG_HEIGHT = 105f;

  /** Number of hotbar slots the background art is drawn for. */
  private static final int SLOT_COUNT = 8;

  /**
   * Gaps (in background-texture pixels) between the edge of the art and the first/last slot window,
   * measured from the source image. The end-caps (scrollwork) live outside these gaps.
   */
  private static final float SLOT_AREA_LEFT = 82f;

  private static final float SLOT_AREA_RIGHT_PAD = BG_WIDTH - 763f; // 90f
  private static final float SLOT_AREA_TOP_PAD = 10f;
  private static final float SLOT_AREA_BOTTOM_PAD = BG_HEIGHT - 86f; // 19f

  /** Size of a single slot window inside the art (derived from the measured region). */
  private static final float SLOT_WIDTH = (763f - SLOT_AREA_LEFT) / SLOT_COUNT; // ~85.1f

  private static final float SLOT_HEIGHT = 86f - SLOT_AREA_TOP_PAD; // 76f

  private static final float ICON_SIZE = 44f;
  private static final int BORDER_THICKNESS = 1;

  /** Alpha of the selection highlight so the underlying art still reads through it. */
  private static final float SELECTION_ALPHA = 0.35f;

  private static NinePatchDrawable cachedSelectionHighlight;

  private Table root;
  private Stack stack;
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
    if (stack != null) {
      stack.setVisible(false);
    }
  }

  /** Displays the quick bar after the backpack is closed. */
  private void showBar() {
    if (stack != null) {
      stack.setVisible(true);
    }
  }

  /** Creates and positions the inventory bar. */
  private void addActors() {
    root = new Table();
    root.bottom();
    root.setFillParent(true);
    root.padBottom(20f);

    stack = new Stack();

    Image background =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset(INVENTORY_BACKGROUND_TEXTURE, Texture.class));
    background.setSize(BG_WIDTH, BG_HEIGHT);
    stack.add(background);

    table = new Table();
    // Pad the table in from the edges of the art so each cell lands directly over
    // one of the drawn slot windows instead of overlapping the scrollwork end-caps.
    table
        .padLeft(SLOT_AREA_LEFT)
        .padRight(SLOT_AREA_RIGHT_PAD)
        .padTop(SLOT_AREA_TOP_PAD)
        .padBottom(SLOT_AREA_BOTTOM_PAD);
    populateSlots();
    stack.add(table);

    root.add(stack).size(BG_WIDTH, BG_HEIGHT);

    stage.addActor(root);
  }

  /** Builds a translucent NinePatch used to highlight the selected slot without hiding the art. */
  static NinePatchDrawable getSelectionHighlightDrawable() {
    if (cachedSelectionHighlight != null) {
      return cachedSelectionHighlight;
    }

    int size = 16;
    int border = BORDER_THICKNESS + 2;

    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pixmap.setColor(new Color(0.95f, 0.85f, 0.55f, SELECTION_ALPHA));
    pixmap.fillRectangle(0, 0, size, size);
    Texture texture = new Texture(pixmap);
    pixmap.dispose();

    NinePatch patch = new NinePatch(texture, border, border, border, border);
    cachedSelectionHighlight = new NinePatchDrawable(patch);
    return cachedSelectionHighlight;
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

      // Slot windows sit flush against each other in the art, so no padding between cells.
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
    slot.pad(6f);

    if (selected) {
      slot.setBackground(getSelectionHighlightDrawable());
    }

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
    slot.pad(6f);

    if (selected) {
      slot.setBackground(getSelectionHighlightDrawable());
    }

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
