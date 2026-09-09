package com.csse3200.game.components.itemdictionary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Displays the player's item dictionary.
 *
 * <p>The dictionary shows all item types. Discovered items display their icon, name and
 * description, while undiscovered items remain hidden.
 */
public class ItemDictionaryDisplay extends UIComponent {
  private Table table;
  private Table contentTable;
  private ItemDictionaryComponent dictionary;
  private boolean visible = false;

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

    refresh();

    table.add(contentTable);
    stage.addActor(table);
  }

  /** Rebuilds the dictionary using the player's current discovery data. */
  private void refresh() {
    if (contentTable == null) {
      return;
    }

    contentTable.clear();

    Label title = new Label("Item Dictionary", skin, "large");

    contentTable.add(title).colspan(3).padBottom(20f);
    contentTable.row();

    for (ItemType itemType : ItemType.values()) {
      addItemEntry(itemType);
    }
  }

  /**
   * Adds one item entry to the dictionary.
   *
   * @param itemType item type represented by this entry
   */
  private void addItemEntry(ItemType itemType) {
    boolean discovered = dictionary != null && dictionary.isDiscovered(itemType);

    if (discovered) {
      addDiscoveredEntry(itemType);
    } else {
      addLockedEntry();
    }

    contentTable.row();
  }

  /**
   * Adds an unlocked item entry containing its icon, name and description.
   *
   * @param itemType discovered item type
   */
  private void addDiscoveredEntry(ItemType itemType) {
    Image icon = null;

    String texturePath = itemType.getTexturePath();

    if (ServiceLocator.getResourceService().containsAsset(texturePath, Texture.class)) {
      Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
      icon = new Image(texture);
    }

    Label name = new Label(itemType.getDisplayName(), skin);
    Label description = new Label(itemType.getDescription(), skin);

    description.setWrap(true);

    if (icon != null) {
      contentTable.add(icon).size(50f, 50f).padRight(15f).padBottom(10f);
    } else {
      contentTable
          .add(new Label("?", skin, "large"))
          .width(50f)
          .height(50f)
          .center()
          .padRight(15f)
          .padBottom(10f);
    }

    contentTable.add(name).width(150f).left().padRight(20f).padBottom(10f);

    contentTable.add(description).width(300f).left().padBottom(10f);
  }

  /** Adds an entry for an item that has not yet been discovered. */
  private void addLockedEntry() {
    Label iconPlaceholder = new Label("?", skin, "large");
    Label name = new Label("???", skin);
    Label description = new Label("Item not discovered", skin);

    contentTable.add(iconPlaceholder).width(50f).height(50f).center().padRight(15f).padBottom(10f);

    contentTable.add(name).width(150f).left().padRight(20f).padBottom(10f);

    contentTable.add(description).width(300f).left().padBottom(10f);
  }

  /** Displays the item dictionary. */
  public void showDictionary() {
    visible = true;
    table.setVisible(true);
    refresh();

    entity.getEvents().trigger("dictionaryOpened");
  }

  /** Hides the item dictionary. */
  public void hideDictionary() {
    visible = false;
    table.setVisible(false);

    entity.getEvents().trigger("dictionaryClosed");
  }

  /** Toggles the item dictionary between visible and hidden. */
  public void toggleDictionary() {
    if (visible) {
      hideDictionary();
    } else {
      showDictionary();
    }
  }

  /**
   * Returns whether the dictionary is visible.
   *
   * @return true if the dictionary is currently displayed
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
