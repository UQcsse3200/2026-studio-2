package com.csse3200.game.components.itemdictionary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
 * Displays the player's item dictionary.
 *
 * <p>The dictionary contains a grid of item entries. Discovered items display their icons and can
 * be clicked to open a detailed information page. Undiscovered items remain locked.
 */
public class ItemDictionaryDisplay extends UIComponent {
  private enum DictionaryPage {
    GRID,
    DETAILS
  }

  private static final int GRID_COLUMNS = 5;

  private Table table;
  private Table contentTable;
  private ItemDictionaryComponent dictionary;

  private boolean visible = false;
  private DictionaryPage currentPage = DictionaryPage.GRID;
  private ItemType selectedItem;

  @Override
  public void create() {
    super.create();

    dictionary = entity.getComponent(ItemDictionaryComponent.class);

    table = new Table();
    table.setFillParent(true);
    table.setVisible(false);

    contentTable = new Table();
    contentTable.setBackground(skin.getDrawable("button-c"));
    contentTable.pad(25f);

    entity.getEvents().addListener("itemDictionaryChanged", this::refresh);

    entity.getEvents().addListener("backpackOpened", this::hideDictionary);

    table.add(contentTable);
    stage.addActor(table);

    showGridPage();
  }

  /** Displays the dictionary grid page. */
  private void showGridPage() {
    currentPage = DictionaryPage.GRID;
    selectedItem = null;

    contentTable.clear();

    Label title = new Label("Item Dictionary", skin, "large");

    contentTable.add(title).colspan(GRID_COLUMNS).padBottom(20f);

    contentTable.row();

    int column = 0;

    for (ItemType itemType : ItemType.values()) {
      Table slot = createDictionarySlot(itemType);

      contentTable.add(slot).size(110f, 110f).pad(8f);

      column++;

      if (column % GRID_COLUMNS == 0) {
        contentTable.row();
      }
    }
  }

  /**
   * Creates one dictionary grid slot.
   *
   * @param itemType item represented by this slot
   * @return created slot
   */
  private Table createDictionarySlot(ItemType itemType) {
    Table slot = new Table();
    slot.setBackground(skin.getDrawable("button-c"));

    boolean discovered = dictionary != null && dictionary.isDiscovered(itemType);

    if (discovered) {
      addDiscoveredSlotContent(slot, itemType);
      addSlotClickListener(slot, itemType);
    } else {
      Label locked = new Label("?", skin, "large");
      slot.add(locked).expand().center();
    }

    return slot;
  }

  /**
   * Adds the icon of a discovered item to a dictionary slot.
   *
   * @param slot slot receiving the item icon
   * @param itemType discovered item type
   */
  private void addDiscoveredSlotContent(Table slot, ItemType itemType) {
    String texturePath = itemType.getTexturePath();

    if (ServiceLocator.getResourceService().containsAsset(texturePath, Texture.class)) {
      Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);

      Image icon = new Image(texture);

      slot.add(icon).size(75f, 75f).expand().center();
    } else {
      Label missingIcon = new Label("?", skin, "large");
      slot.add(missingIcon).expand().center();
    }
  }

  /**
   * Makes a discovered item slot clickable.
   *
   * @param slot slot receiving the click listener
   * @param itemType item opened when clicked
   */
  private void addSlotClickListener(Table slot, ItemType itemType) {
    slot.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            showDetailsPage(itemType);
          }
        });
  }

  /**
   * Displays the detailed page for an item.
   *
   * @param itemType item to display
   */
  private void showDetailsPage(ItemType itemType) {
    if (itemType == null || dictionary == null || !dictionary.isDiscovered(itemType)) {
      return;
    }

    currentPage = DictionaryPage.DETAILS;
    selectedItem = itemType;

    contentTable.clear();

    Label title = new Label(itemType.getDisplayName(), skin, "large");

    contentTable.add(title).padBottom(20f);
    contentTable.row();

    addDetailsIcon(itemType);

    contentTable.row();

    Label description = new Label(itemType.getDescription(), skin);

    description.setWrap(true);

    contentTable.add(description).width(350f).left().padTop(15f).padBottom(20f);

    contentTable.row();

    addItemStats(itemType);

    contentTable.row();

    Label back = new Label("< Back", skin);

    back.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            showGridPage();
          }
        });

    contentTable.add(back).padTop(20f);
  }

  /**
   * Adds the selected item's icon to the details page.
   *
   * @param itemType selected item
   */
  private void addDetailsIcon(ItemType itemType) {
    String texturePath = itemType.getTexturePath();

    if (ServiceLocator.getResourceService().containsAsset(texturePath, Texture.class)) {
      Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);

      Image icon = new Image(texture);

      contentTable.add(icon).size(120f, 120f);
    } else {
      contentTable.add(new Label("?", skin, "large")).size(120f, 120f);
    }
  }

  /**
   * Adds basic item statistics to the details page.
   *
   * @param itemType selected item
   */
  private void addItemStats(ItemType itemType) {
    Table statsTable = new Table();

    statsTable.setBackground(skin.getDrawable("button-c"));
    statsTable.pad(15f);

    if (itemType.getDamage() > 0) {
      statsTable.add(new Label("Damage: " + itemType.getDamage(), skin)).left();

      statsTable.row();
    }

    if (itemType.getRange() > 0f) {
      statsTable.add(new Label("Range: " + itemType.getRange(), skin)).left();

      statsTable.row();
    }

    if (itemType.getCooldown() > 0f) {
      statsTable.add(new Label("Cooldown: " + itemType.getCooldown(), skin)).left();

      statsTable.row();
    }

    if (itemType.getHealAmount() > 0) {
      statsTable.add(new Label("Heal: " + itemType.getHealAmount(), skin)).left();

      statsTable.row();
    }

    if (itemType.getBurnDamagePerSecond() > 0f) {
      statsTable.add(new Label("Burn Damage: " + itemType.getBurnDamagePerSecond(), skin)).left();

      statsTable.row();
    }

    if (itemType.getBurnTime() > 0f) {
      statsTable.add(new Label("Burn Time: " + itemType.getBurnTime(), skin)).left();

      statsTable.row();
    }

    if (itemType.getSlowSpeed() > 0f) {
      statsTable.add(new Label("Slow Speed: " + itemType.getSlowSpeed(), skin)).left();

      statsTable.row();
    }

    if (itemType.getSlowTime() > 0f) {
      statsTable.add(new Label("Slow Time: " + itemType.getSlowTime(), skin)).left();

      statsTable.row();
    }

    contentTable.add(statsTable).width(300f);
  }

  /** Refreshes the currently displayed dictionary page. */
  private void refresh() {
    if (contentTable == null) {
      return;
    }

    if (currentPage == DictionaryPage.DETAILS
        && selectedItem != null
        && dictionary != null
        && dictionary.isDiscovered(selectedItem)) {
      showDetailsPage(selectedItem);
    } else {
      showGridPage();
    }
  }

  /** Displays the item dictionary. */
  public void showDictionary() {
    visible = true;

    currentPage = DictionaryPage.GRID;
    selectedItem = null;

    table.setVisible(true);

    showGridPage();

    entity.getEvents().trigger("dictionaryOpened");
  }

  /** Hides the item dictionary. */
  public void hideDictionary() {
    visible = false;

    table.setVisible(false);

    currentPage = DictionaryPage.GRID;
    selectedItem = null;

    entity.getEvents().trigger("dictionaryClosed");
  }

  /** Toggles the dictionary between visible and hidden. */
  public void toggleDictionary() {
    if (visible) {
      hideDictionary();
    } else {
      showDictionary();
    }
  }

  /**
   * Returns whether the dictionary is currently visible.
   *
   * @return true if visible
   */
  public boolean isDictionaryVisible() {
    return visible;
  }

  @Override
  public void update() {
    super.update();

    if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
      toggleDictionary();
    }
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
