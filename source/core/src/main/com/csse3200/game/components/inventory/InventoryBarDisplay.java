package com.csse3200.game.components.inventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
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

  private static final int BORDER_THICKNESS = 1;

  /*
   * Inventory bar dimensions.
   *
   * These should match the size of the background inside the Stack.
   */
  private static final float BAR_WIDTH = 1200f;
  private static final float BAR_HEIGHT = 400f;

  /*
   * Individual slot dimensions.
   */
  private static final float SLOT_WIDTH = 60f;
  private static final float SLOT_HEIGHT = 55f;

  /*
   * Position of the first inventory slot relative to the
   * bottom-left corner of the inventory background.
   *
   * Adjust these values to line the slots up with your new
   * pixel-art inventory background.
   */
  private static final float SLOT_START_X = 95f;
  private static final float SLOT_START_Y = 69f;

  /*
   * Distance between the beginning of one slot and the next.
   *
   * 60 width + 10 gap = 70.
   */
  private static final float SLOT_SPACING = 70f;

  private static NinePatchDrawable cachedBackground;

  private Table root;
  private Stack stack;
  private Group slotGroup;

  @Override
  public void create() {
    super.create();

    entity.getEvents().addListener("inventoryChanged", this::refresh);

    entity.getEvents().addListener("inventorySelectionChanged", this::refresh);

    entity.getEvents().addListener("backpackOpened", this::hideBar);

    entity.getEvents().addListener("backpackClosed", this::showBar);

    addActors();
  }

  /**
   * Gets the texture belonging to an item.
   *
   * @param itemType item type
   * @return texture path
   */
  private String getItemTexture(ItemType itemType) {
    return itemType.getTexturePath();
  }

  /** Refreshes the inventory bar when the inventory changes. */
  private void refresh() {
    if (slotGroup != null) {
      populateSlots();
    }
  }

  /** Hides the entire quick bar while the backpack is open. */
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

    /*
     * Root table positions the entire inventory bar
     * at the bottom of the screen.
     */
    root = new Table();

    root.bottom();
    root.setFillParent(true);
    root.padBottom(20f);

    /*
     * Stack allows the slots to be rendered over
     * the inventory background.
     */
    stack = new Stack();

    /*
     * Inventory background.
     */
    Texture backgroundTexture =
        ServiceLocator.getResourceService().getAsset(INVENTORY_BACKGROUND_TEXTURE, Texture.class);

    Image background = new Image(backgroundTexture);

    /*
     * Background can still use a Table because we
     * only need to position one large image.
     */
    Table backgroundTable = new Table();

    backgroundTable.add(background).size(BAR_WIDTH, BAR_HEIGHT);

    stack.add(backgroundTable);

    /*
     * Slot Group.
     *
     * Group is used because each inventory slot needs
     * to line up precisely with the artwork in the
     * background image.
     */
    slotGroup = new Group();

    /*
     * Give the Group the same coordinate space
     * as the background.
     */
    slotGroup.setSize(BAR_WIDTH, BAR_HEIGHT);

    /*
     * Create slots before displaying the Group.
     */
    populateSlots();

    /*
     * Group is added after the background so the
     * slots are drawn on top.
     */
    stack.add(slotGroup);

    /*
     * Give the Stack a known size.
     */
    root.add(stack).size(BAR_WIDTH, BAR_HEIGHT);

    stage.addActor(root);
  }

  /** Populates the inventory bar with occupied and empty slots. */
  private void populateSlots() {

    /*
     * Remove old slot actors before rebuilding.
     */
    slotGroup.clearChildren();

    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);

    if (inventory == null) {
      return;
    }

    /*
     * Create every hotbar slot.
     */
    for (int slotIndex = 0; slotIndex < inventory.getHotbarSlotCount(); slotIndex++) {

      int slotNumber = slotIndex + 1;

      boolean selected = inventory.getSelectedSlotIndex() == slotIndex;

      InventorySlot inventorySlot = inventory.getSlot(slotIndex);

      Table slot;

      /*
       * Empty slot.
       */
      if (inventorySlot == null || inventorySlot.isEmpty()) {

        slot = createEmptySlot(slotNumber, selected);
      }

      /*
       * Occupied slot.
       */
      else {

        slot =
            createSlot(
                slotNumber, inventorySlot.getItemType(), inventorySlot.getQuantity(), selected);
      }

      /*
       * Explicitly set the slot size.
       *
       * Group does not automatically size its children
       * like a Table does.
       */
      slot.setSize(SLOT_WIDTH, SLOT_HEIGHT);

      /*
       * Explicitly calculate the slot position.
       *
       * Slot 0:
       * X = SLOT_START_X
       *
       * Slot 1:
       * X = SLOT_START_X + SLOT_SPACING
       *
       * Slot 2:
       * X = SLOT_START_X + SLOT_SPACING * 2
       */
      float slotX = SLOT_START_X + slotIndex * SLOT_SPACING;

      float slotY = SLOT_START_Y;

      slot.setPosition(slotX, slotY);

      /*
       * Add the positioned slot to the Group.
       */
      slotGroup.addActor(slot);
    }
  }

  /**
   * Creates the drawable used to highlight the selected inventory slot.
   *
   * @return selection background drawable
   */
  private static NinePatchDrawable getBackgroundDrawable() {

    if (cachedBackground != null) {
      return cachedBackground;
    }

    int size = 16;

    int border = BORDER_THICKNESS + 2;

    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

    pixmap.setColor(new Color(0.88f, 0.83f, 0.55f, 1f));

    for (int i = 0; i < border; i++) {

      pixmap.drawRectangle(i, i, size - i * 2, size - i * 2);
    }

    Texture texture = new Texture(pixmap);

    pixmap.dispose();

    NinePatch patch = new NinePatch(texture, border, border, border, border);

    cachedBackground = new NinePatchDrawable(patch);

    return cachedBackground;
  }

  /**
   * Creates one occupied inventory slot.
   *
   * @param slotNumber slot number displayed to the player
   * @param item item stored in the slot
   * @param count quantity of the item
   * @param selected whether this slot is currently selected
   * @return created slot
   */
  private Table createSlot(int slotNumber, ItemType item, int count, boolean selected) {

    Table slot = new Table();

    /*
     * Smaller padding is useful here because the entire
     * slot is only 60 x 55.
     */
    slot.pad(4f);

    /*
     * Highlight the selected slot.
     */
    if (selected) {
      slot.setBackground(getBackgroundDrawable());
    }

    /*
     * Item icon.
     */
    Texture texture =
        ServiceLocator.getResourceService().getAsset(getItemTexture(item), Texture.class);

    Image icon = new Image(texture);

    /*
     * Quantity label.
     */
    Label countLabel =
        new Label("x" + count, new Label.LabelStyle(skin.getFont("font"), Color.WHITE));

    countLabel.setColor(Color.WHITE);

    /*
     * Icon occupies the main section.
     */
    slot.add(icon).size(36f, 36f).expand().center();

    slot.row();

    /*
     * Quantity appears underneath.
     */
    slot.add(countLabel).right().padRight(2f).padBottom(1f);

    return slot;
  }

  /**
   * Creates an empty inventory slot.
   *
   * @param slotNumber slot number
   * @param selected whether this slot is selected
   * @return created empty slot
   */
  private Table createEmptySlot(int slotNumber, boolean selected) {

    Table slot = new Table();

    slot.pad(4f);

    /*
     * Selection outline still appears even if
     * the selected slot is empty.
     */
    if (selected) {
      slot.setBackground(getBackgroundDrawable());
    }

    /*
     * Empty cell fills the available slot area.
     */
    slot.add().expand().fill();

    return slot;
  }

  @Override
  public void draw(SpriteBatch batch) {
    // Drawing is handled by the Scene2D stage.
  }

  @Override
  public void dispose() {
    super.dispose();

    /*
     * Removing root removes the entire inventory UI,
     * including its Stack and slot Group.
     */
    if (root != null) {
      root.remove();
    }
  }
}
