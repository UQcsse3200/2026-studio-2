package com.csse3200.game.components.shop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shop page overlay opened by interacting with a shopkeeper NPC.
 *
 * <p>Shows catalog items, current gold, and buy buttons. Purchases are handled by ShopComponent.
 */
public class ShopDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(ShopDisplay.class);
  private static final float Z_INDEX = 3f;
  private static final float ICON_SIZE = 40f;

  private Table table;
  private Table listingsTable;
  private Label goldLabel;
  private Label statusLabel;
  private boolean open;

  @Override
  public void create() {
    super.create();
    addActors();
    entity.getEvents().addListener("openShop", this::open);
    entity.getEvents().addListener("closeShop", this::close);
    entity.getEvents().addListener("itemPurchased", this::onItemPurchased);
    entity.getEvents().addListener("purchaseFailed", this::onPurchaseFailed);
    entity.getEvents().addListener("inventoryChanged", this::refreshIfOpen);
    entity.getEvents().addListener("goldChanged", this::refreshIfOpen);
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);
    table.setVisible(false);

    Table panel = new Table();
    panel.setBackground(skin.getDrawable("window-c"));
    panel.pad(30f);

    Label title = new Label("Shop", skin, "title");
    goldLabel = new Label(goldText(), skin);
    listingsTable = new Table();
    statusLabel = new Label("", skin);

    TextButton closeBtn = new TextButton("Close", skin);
    closeBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Shop close button clicked");
            entity.getEvents().trigger("closeShop");
          }
        });

    panel.add(title);
    panel.row();
    panel.add(goldLabel).padTop(10f);
    panel.row();
    panel.add(listingsTable).padTop(20f);
    panel.row();
    panel.add(statusLabel).padTop(16f);
    panel.row();
    panel.add(closeBtn).padTop(24f);

    table.add(panel);
    stage.addActor(table);
    refreshListings();
  }

  /** Shows the shop page and pauses the game so the player can use the UI. */
  public void open() {
    if (open) {
      return;
    }
    open = true;
    statusLabel.setText("");
    refresh();
    table.setVisible(true);
    setGamePaused(true);
    logger.info("Opened shop");
  }

  /** Hides the shop page and resumes the game. */
  public void close() {
    if (!open) {
      return;
    }
    open = false;
    table.setVisible(false);
    setGamePaused(false);
    logger.info("Closed shop");
  }

  /**
   * @return true if the shop page is currently visible
   */
  public boolean isOpen() {
    return open;
  }

  private void onItemPurchased(ItemType itemType) {
    statusLabel.setText("Purchased " + itemType.getDisplayName() + ".");
    refresh();
  }

  private void onPurchaseFailed(String reason) {
    statusLabel.setText(reason);
    refresh();
  }

  private void refreshIfOpen() {
    if (open) {
      refresh();
    }
  }

  private void refresh() {
    goldLabel.setText(goldText());
    refreshListings();
  }

  private void refreshListings() {
    listingsTable.clearChildren();
    ShopComponent shop = entity.getComponent(ShopComponent.class);

    for (ShopListing listing : ShopCatalog.getListings()) {
      listingsTable.add(createListingRow(listing, shop)).growX().padBottom(8f);
      listingsTable.row();
    }
  }

  private Table createListingRow(ShopListing listing, ShopComponent shop) {
    Table row = new Table();
    row.setBackground(skin.getDrawable("button-c"));
    row.pad(8f);

    Texture texture = getItemTexture(listing.getItemType());
    if (texture != null) {
      row.add(new Image(texture)).size(ICON_SIZE, ICON_SIZE).padRight(12f);
    }

    row.add(new Label(listing.getItemType().getDisplayName(), skin)).width(180f).left();
    row.add(new Label("x" + listing.getQuantity(), skin)).width(50f);
    row.add(new Label(listing.getPrice() + "g", skin)).width(60f).padRight(12f);

    TextButton buyBtn = new TextButton("Buy", skin);
    buyBtn.setDisabled(shop == null || !shop.canBuy(listing));
    buyBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            if (shop != null) {
              shop.buy(listing);
            }
          }
        });
    row.add(buyBtn).width(90f);
    return row;
  }

  private Texture getItemTexture(ItemType itemType) {
    if (ServiceLocator.getResourceService() == null
        || !ServiceLocator.getResourceService()
            .containsAsset(itemType.getTexturePath(), Texture.class)) {
      return null;
    }
    return ServiceLocator.getResourceService().getAsset(itemType.getTexturePath(), Texture.class);
  }

  private String goldText() {
    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
    int gold = inventory == null ? 0 : inventory.getGold();
    return "Gold: " + gold;
  }

  private void setGamePaused(boolean paused) {
    if (ServiceLocator.getEntityService() != null) {
      ServiceLocator.getEntityService().setPaused(paused);
    }
  }

  @Override
  public void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  @Override
  public void dispose() {
    if (table != null) {
      table.remove();
    }
    super.dispose();
  }
}
