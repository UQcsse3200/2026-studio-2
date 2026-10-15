package com.csse3200.game.components.shop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.inventory.InventorySlotStyle;
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
  private final InventorySlotStyle slotStyle = new InventorySlotStyle();
  private static final Logger logger = LoggerFactory.getLogger(ShopDisplay.class);
  private static final float Z_INDEX = 3f;
  private static final float ICON_SIZE = 40f;
  private static final String SHOP_BACKGROUND = "images/backgrounds/shop_bg.png";
  private static final Color PRICE_TEXT_COLOR = new Color(1f, 0.78f, 0.1f, 1f);
  private static final Color COUNTER_TEXT_COLOR = new Color(0.25f, 0.12f, 0.05f, 1f);

  // Layout of shop_bg.png as fractions of its width/height: the counter band that holds the gold
  // and status text, the dark panel that holds the listings, and the plaques for the buttons.
  private static final float SCENE_COUNTER_TOP = 0.60f;
  private static final float SCENE_COUNTER_HEIGHT = 0.165f;
  private static final float SCENE_PANEL_LEFT = 0.075f;
  private static final float SCENE_GOLD_WIDTH = 0.25f;
  private static final float SCENE_COUNTER_RIGHT_MARGIN = 0.07f;
  private static final float SCENE_PANEL_WIDTH = 0.625f;
  private static final float SCENE_PANEL_HEIGHT = 0.18f;
  private static final float SCENE_PLAQUE_COLUMN_LEFT = 0.744f;
  private static final float SCENE_PLAQUE_WIDTH = 0.186f;
  private static final float SCENE_PLAQUE_HEIGHT = 0.077f;
  private static final float SCENE_PLAQUE_GAP = 0.024f;
  private static final int LISTING_COLUMNS = 3;
  private static final float REFERENCE_HEIGHT = 720f;
  // How much of the screen the shop window fills, leaving a margin around it.
  private static final float SCENE_SCREEN_FRACTION = 0.85f;
  private static final Color DIM_COLOR = new Color(0f, 0f, 0f, 0.8f);

  private Table table;
  private Table listingsTable;
  private Label goldLabel;
  private Label statusLabel;
  private TextButton closeBtn;
  private TextButton blackjackBtn;
  private Texture backgroundTexture;
  private Table panel;
  private Texture dimTexture;
  private float uiScale = 1f;
  private float listingTileWidth;
  private float listingTileHeight;
  private boolean open;
  private boolean dirty;

  private final Label.LabelStyle white = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
  private final Label.LabelStyle whiteLarge =
      new Label.LabelStyle(skin.get("large", Label.LabelStyle.class));

  private final TextButton.TextButtonStyle darkStyle =
      new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));

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

    whiteLarge.fontColor = Color.WHITE;
    white.fontColor = Color.WHITE;
    goldLabel = new Label(goldText(), white);
    listingsTable = new Table();
    statusLabel = new Label("", white);

    closeBtn = new TextButton("Close", darkStyle);
    closeBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Shop close button clicked");
            entity.getEvents().trigger("closeShop");
          }
        });

    blackjackBtn = new TextButton("Play Blackjack", skin);
    blackjackBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.info("Blackjack button clicked");
            close();
            entity.getEvents().trigger("openBlackjack");
          }
        });

    stage.addActor(table);
    buildLayout();
  }

  /**
   * Lays the shop widgets out over the shop background art, or in a plain panel when the background
   * texture has not been loaded.
   */
  private void buildLayout() {
    table.clearChildren();
    backgroundTexture = getTexture(SHOP_BACKGROUND);
    if (backgroundTexture != null) {
      buildThemedLayout(backgroundTexture);
    } else {
      buildPlainLayout();
    }
    refreshListings();
  }

  private void buildPlainLayout() {
    Label title = new Label("Shop", whiteLarge);
    goldLabel.setStyle(white);
    statusLabel.setStyle(white);
    listingsTable.clear();
    table.setBackground((Drawable) null);

    panel = new Table();
    panel.setBackground(slotStyle.getDarkerBox());
    panel.pad(30f);
    panel.add(title);
    panel.row();
    panel.add(goldLabel).padTop(10f);
    panel.row();
    panel.add(listingsTable).padTop(20f);
    panel.row();
    panel.add(blackjackBtn).width(220f).padTop(20f);
    panel.row();
    panel.add(statusLabel).padTop(16f);
    panel.row();
    panel.add(closeBtn).padTop(24f);

    table.add(panel);
  }

  /** Fits shop_bg.png to the stage and places the shop widgets over its panels. */
  private void buildThemedLayout(Texture background) {
    float scale =
        SCENE_SCREEN_FRACTION
            * Math.min(
                stage.getWidth() / background.getWidth(),
                stage.getHeight() / background.getHeight());
    float w = background.getWidth() * scale;
    float h = background.getHeight() * scale;
    uiScale = h / REFERENCE_HEIGHT;

    table.setBackground(getDimDrawable());

    Label.LabelStyle counterStyle = new Label.LabelStyle(white);
    counterStyle.fontColor = COUNTER_TEXT_COLOR;
    goldLabel.setStyle(counterStyle);
    goldLabel.setFontScale(1.1f * uiScale);
    statusLabel.setStyle(counterStyle);
    statusLabel.setFontScale(1.1f * uiScale);

    styleButtonAsPlaque(closeBtn);
    styleButtonAsPlaque(blackjackBtn);
    blackjackBtn.setText("Blackjack");

    float panelLeft = SCENE_PANEL_LEFT * w;
    float panelWidth = SCENE_PANEL_WIDTH * w;
    float panelHeight = SCENE_PANEL_HEIGHT * h;
    float plaqueColumnLeft = SCENE_PLAQUE_COLUMN_LEFT * w;
    Table scene = new Table();
    scene.setBackground(new TextureRegionDrawable(new TextureRegion(background)));
    scene.top().left();

    scene.add().height(SCENE_COUNTER_TOP * h).colspan(2).row();

    // Gold on the left and the status message beside it, both kept inside the counter.
    float counterRight = w - SCENE_COUNTER_RIGHT_MARGIN * w;
    float goldWidth = SCENE_GOLD_WIDTH * w;
    statusLabel.setWrap(true);
    Table counter = new Table();
    counter.left();
    counter.add(goldLabel).width(goldWidth).left();
    counter.add(statusLabel).width(counterRight - panelLeft - goldWidth).left();
    scene.add(counter).colspan(2).height(SCENE_COUNTER_HEIGHT * h).padLeft(panelLeft).left();
    scene.row();

    scene
        .add(listingsTable)
        .width(panelWidth)
        .height(panelHeight)
        .padLeft(panelLeft)
        .padRight(plaqueColumnLeft - panelLeft - panelWidth)
        .top()
        .left();

    Table plaques = new Table();
    plaques.top().left();
    plaques
        .add(blackjackBtn)
        .size(SCENE_PLAQUE_WIDTH * w, SCENE_PLAQUE_HEIGHT * h)
        .padBottom(SCENE_PLAQUE_GAP * h);
    plaques.row();
    plaques.add(closeBtn).size(SCENE_PLAQUE_WIDTH * w, SCENE_PLAQUE_HEIGHT * h);
    scene.add(plaques).top().left().padTop(0.004f * h);

    table.add(scene).size(w, h);
    listingsTable.clear();
    listingsTable.top().left();
    listingTileWidth = panelWidth / LISTING_COLUMNS;
    listingTileHeight = panelHeight / 2f;
  }

  private Drawable getDimDrawable() {
    if (dimTexture == null) {
      Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
      pixmap.setColor(DIM_COLOR);
      pixmap.fill();
      dimTexture = new Texture(pixmap);
      pixmap.dispose();
    }
    return new TextureRegionDrawable(new TextureRegion(dimTexture));
  }

  /** Makes a button transparent so the plaque painted into the background shows through. */
  private void styleButtonAsPlaque(TextButton button) {
    TextButton.TextButtonStyle plaque =
        new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
    plaque.up = null;
    plaque.down = null;
    plaque.over = null;
    plaque.disabled = null;
    plaque.fontColor = COUNTER_TEXT_COLOR;
    plaque.overFontColor = Color.WHITE;
    plaque.downFontColor = Color.WHITE;
    button.setStyle(plaque);
    button.getLabel().setFontScale(0.8f * uiScale);
  }

  /** Shows the shop page and pauses the game so the player can use the UI. */
  public void open() {
    if (open) {
      return;
    }
    open = true;
    statusLabel.setText("");
    buildLayout();
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
    dirty = true;
  }

  private void onPurchaseFailed(String reason) {
    statusLabel.setText(reason);
    dirty = true;
  }

  private void refreshIfOpen() {
    if (open) {
      dirty = true;
    }
  }

  private void refresh() {
    dirty = false;
    goldLabel.setText(goldText());
    refreshListings();
  }

  private void refreshListings() {
    listingsTable.clearChildren();
    ShopComponent shop = entity.getComponent(ShopComponent.class);

    darkStyle.up = slotStyle.getDarkerBox();
    darkStyle.down = slotStyle.getSelectedBox();
    darkStyle.over = slotStyle.getNormalBox(); // optional hover feedback
    darkStyle.disabled = slotStyle.getDarkerBox(); // see below
    darkStyle.fontColor = Color.WHITE;

    boolean themed = backgroundTexture != null;
    int index = 0;
    for (ShopListing listing : ShopCatalog.getListings()) {
      if (themed) {
        listingsTable
            .add(createListingTile(listing, shop))
            .size(listingTileWidth, listingTileHeight);
        index++;
        if (index % LISTING_COLUMNS == 0) {
          listingsTable.row();
        }
      } else {
        listingsTable.add(createListingRow(listing, shop)).growX().padBottom(8f);
        listingsTable.row();
      }
    }
  }

  /**
   * Compact listing used over the shop background: the icon beside the name, quantity and price,
   * with the buy button underneath and lined up with the name.
   */
  private Table createListingTile(ShopListing listing, ShopComponent shop) {
    float iconSize = listingTileHeight * 0.4f;
    float buyWidth = listingTileWidth * 0.24f;
    float buyHeight = listingTileHeight * 0.3f;
    float gap = 8f * uiScale;

    Label name = new Label(listing.getItemType().getDisplayName(), white);
    name.setFontScale(0.55f * uiScale);
    name.setEllipsis(true);
    Label quantity = new Label("x" + listing.getQuantity(), white);
    quantity.setFontScale(0.5f * uiScale);
    Label.LabelStyle priceStyle = new Label.LabelStyle(white);
    priceStyle.fontColor = PRICE_TEXT_COLOR;
    Label price = new Label(listing.getPrice() + "g", priceStyle);
    price.setFontScale(0.5f * uiScale);

    Table details = new Table();
    details.add(name).colspan(2).left().growX().row();
    details.add(quantity).left().padRight(gap * 1.5f);
    details.add(price).left().expandX().left();

    Table header = new Table();
    Texture texture = getItemTexture(listing.getItemType());
    if (texture != null) {
      header.add(new Image(texture)).size(iconSize).padRight(gap);
    } else {
      header.add().width(iconSize).padRight(gap);
    }
    header.add(details).left().growX();

    TextButton buyBtn = createBuyButton(listing, shop);
    buyBtn.getLabel().setFontScale(0.6f * uiScale);

    Table tile = new Table();
    tile.pad(gap);
    tile.add(header).left().growX().row();
    tile.add(buyBtn).size(buyWidth, buyHeight).left().padLeft(iconSize + gap).padTop(gap / 2f);
    return tile;
  }

  private TextButton createBuyButton(ShopListing listing, ShopComponent shop) {
    TextButton buyBtn = new TextButton("Buy", darkStyle);
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
    return buyBtn;
  }

  private Table createListingRow(ShopListing listing, ShopComponent shop) {
    Table row = new Table();
    row.setBackground(slotStyle.getNormalBox());
    row.pad(8f);

    Texture texture = getItemTexture(listing.getItemType());
    if (texture != null) {
      row.add(new Image(texture)).size(ICON_SIZE, ICON_SIZE).padRight(12f);
    }

    white.fontColor = Color.WHITE;

    row.add(new Label(listing.getItemType().getDisplayName(), white)).width(180f).left();
    row.add(new Label("x" + listing.getQuantity(), white)).width(50f);
    row.add(new Label(listing.getPrice() + "g", white)).width(60f).padRight(12f);

    row.add(createBuyButton(listing, shop)).width(90f);
    return row;
  }

  private Texture getItemTexture(ItemType itemType) {
    return getTexture(itemType.getTexturePath());
  }

  /** Returns the texture if it has been loaded, or null so the UI can fall back gracefully. */
  private Texture getTexture(String path) {
    if (ServiceLocator.getResourceService() == null
        || !ServiceLocator.getResourceService().containsAsset(path, Texture.class)) {
      return null;
    }
    return ServiceLocator.getResourceService().getAsset(path, Texture.class);
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
    // Render callbacks still run while gameplay is paused by this overlay.
    if (open && dirty) {
      refresh();
    }
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
    slotStyle.dispose();
    if (dimTexture != null) {
      dimTexture.dispose();
      dimTexture = null;
    }
    super.dispose();
  }
}
