package com.csse3200.game.components.shop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
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
  private static final Color GOLD_TEXT_COLOR = new Color(1f, 0.78f, 0.1f, 1f);
  // Text on the wooden plaques is dark brown; text in the dark panel is light.
  private static final Color COUNTER_TEXT_COLOR = new Color(0.25f, 0.12f, 0.05f, 1f);
  private static final Color DESCRIPTION_TEXT_COLOR = new Color(0.8f, 0.8f, 0.8f, 1f);
  private static final String GREETING = "Welcome, traveller! What can I get for you?";
  private static final String SELECT_HINT = "Select an item on the counter to see its details.";
  // Share of the dark panel taken by the gold and message column, the rest shows item details.
  private static final float PANEL_MESSAGE_SHARE = 0.36f;
  // Size of an item's icon on the counter, as a share of its slot.
  private static final float ITEM_ICON_SHARE = 0.9f;

  // Layout of shop_bg.png as fractions of its width/height: the counter the items are laid out on,
  // the dark panel that shows the gold and the merchant's messages, and the plaques for the
  // buttons.
  private static final float SCENE_TABLE_TOP = 0.535f;
  private static final float SCENE_TABLE_HEIGHT = 0.228f;
  // Height of one item's slot. It is about the icon's size, so the icons rest on the tabletop.
  private static final float SCENE_ITEM_HEIGHT = 0.14f;
  private static final float SCENE_TABLE_LEFT = 0.09f;
  private static final float SCENE_TABLE_WIDTH = 0.82f;
  private static final float SCENE_PANEL_LEFT = 0.075f;
  private static final float SCENE_PANEL_WIDTH = 0.625f;
  private static final float SCENE_PANEL_HEIGHT = 0.18f;
  private static final float SCENE_PLAQUE_COLUMN_LEFT = 0.744f;
  private static final float SCENE_PLAQUE_WIDTH = 0.186f;
  private static final float SCENE_PLAQUE_HEIGHT = 0.077f;
  private static final float SCENE_PLAQUE_GAP = 0.024f;
  private static final int LISTING_COLUMNS = 6;
  private static final float REFERENCE_HEIGHT = 720f;
  // How much of the screen the shop window fills, leaving a margin around it.
  private static final float SCENE_SCREEN_FRACTION = 0.85f;
  private static final Color DIM_COLOR = new Color(0f, 0f, 0f, 0.8f);

  private Table table;
  private Table listingsTable;
  private Label goldLabel;
  private Label statusLabel;
  private Table detailsTable;
  private ShopListing selectedListing;
  private float detailsBuyWidth;
  private float detailsBuyHeight;
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

    Label.LabelStyle goldStyle = new Label.LabelStyle(white);
    goldStyle.fontColor = GOLD_TEXT_COLOR;
    goldLabel.setStyle(goldStyle);
    goldLabel.setFontScale(1.2f * uiScale);
    statusLabel.setStyle(white);
    statusLabel.setFontScale(0.9f * uiScale);
    statusLabel.setWrap(true);

    styleButtonAsPlaque(closeBtn);
    styleButtonAsPlaque(blackjackBtn);
    blackjackBtn.setText("Blackjack");

    float panelLeft = SCENE_PANEL_LEFT * w;
    float panelWidth = SCENE_PANEL_WIDTH * w;
    float panelHeight = SCENE_PANEL_HEIGHT * h;
    float panelPadding = 0.02f * h;
    float plaqueColumnLeft = SCENE_PLAQUE_COLUMN_LEFT * w;
    float tableWidth = SCENE_TABLE_WIDTH * w;
    float tableHeight = SCENE_TABLE_HEIGHT * h;
    Table scene = new Table();
    scene.setBackground(new TextureRegionDrawable(new TextureRegion(background)));
    scene.top().left();

    scene.add().height(SCENE_TABLE_TOP * h).colspan(2).row();

    // The items are laid out in a row along the counter the merchant is leaning on.
    scene
        .add(listingsTable)
        .colspan(2)
        .width(tableWidth)
        .height(tableHeight)
        .padLeft(SCENE_TABLE_LEFT * w)
        .top()
        .left();
    scene.row();

    // The dark panel shows the player's gold and the merchant's messages on the left, and the
    // details of the selected item, with its price and buy button, on the right.
    float innerWidth = panelWidth - panelPadding * 2f;
    float messageWidth = innerWidth * PANEL_MESSAGE_SHARE;
    float detailsWidth = innerWidth - messageWidth - panelPadding;
    detailsBuyWidth = detailsWidth * 0.3f;
    detailsBuyHeight = panelHeight * 0.22f;

    Table messageColumn = new Table();
    messageColumn.top().left();
    messageColumn.add(goldLabel).left().row();
    messageColumn.add(statusLabel).width(messageWidth).left().padTop(panelPadding / 2f);

    detailsTable = new Table();
    detailsTable.top().left();

    Table infoPanel = new Table();
    infoPanel.top().left().pad(panelPadding);
    infoPanel.add(messageColumn).width(messageWidth).top().left().padRight(panelPadding);
    infoPanel.add(detailsTable).width(detailsWidth).top().left();
    scene
        .add(infoPanel)
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
    listingTileWidth = tableWidth / LISTING_COLUMNS;
    listingTileHeight = SCENE_ITEM_HEIGHT * h;
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
    selectedListing = null;
    buildLayout();
    statusLabel.setText(backgroundTexture != null ? GREETING : "");
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

    if (backgroundTexture == null) {
      for (ShopListing listing : ShopCatalog.getListings()) {
        listingsTable.add(createListingRow(listing, shop)).growX().padBottom(8f);
        listingsTable.row();
      }
      return;
    }

    ButtonGroup<Button> group = new ButtonGroup<>();
    group.setMinCheckCount(0);
    group.setMaxCheckCount(1);
    int index = 0;
    for (ShopListing listing : ShopCatalog.getListings()) {
      Button item = createListingItem(listing);
      group.add(item);
      listingsTable.add(item).size(listingTileWidth, listingTileHeight);
      index++;
      if (index % LISTING_COLUMNS == 0) {
        listingsTable.row();
      }
    }
    updateDetails();
  }

  /**
   * An item laid out on the counter: just its icon, which can be clicked to select it. The selected
   * item is highlighted and its details are shown in the dark panel.
   */
  private Button createListingItem(ShopListing listing) {
    Button.ButtonStyle style = new Button.ButtonStyle();
    style.checked = slotStyle.getSelectedBox();

    Button item = new Button(style);
    Texture texture = getItemTexture(listing.getItemType());
    if (texture != null) {
      float iconSize = ITEM_ICON_SHARE * Math.min(listingTileWidth, listingTileHeight);
      item.add(new Image(texture)).size(iconSize).expand().center();
    }

    // Restore the selection after the items are rebuilt without announcing it as a new click.
    if (listing.equals(selectedListing)) {
      item.setProgrammaticChangeEvents(false);
      item.setChecked(true);
      item.setProgrammaticChangeEvents(true);
    }
    item.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            selectedListing = item.isChecked() ? listing : null;
            updateDetails();
          }
        });
    return item;
  }

  /**
   * Shows the selected item's name, description, quantity, price and buy button in the dark panel,
   * or a hint to pick an item when nothing is selected.
   */
  private void updateDetails() {
    if (detailsTable == null || backgroundTexture == null) {
      return;
    }
    detailsTable.clearChildren();

    if (selectedListing == null) {
      Label hint = new Label(SELECT_HINT, white);
      hint.setFontScale(0.75f * uiScale);
      hint.setWrap(true);
      detailsTable.add(hint).growX();
      return;
    }

    ItemType itemType = selectedListing.getItemType();
    float gap = 6f * uiScale;

    Label name = new Label(itemType.getDisplayName(), white);
    name.setFontScale(1f * uiScale);
    Label.LabelStyle descriptionStyle = new Label.LabelStyle(white);
    descriptionStyle.fontColor = DESCRIPTION_TEXT_COLOR;
    Label description = new Label(itemType.getDescription(), descriptionStyle);
    description.setFontScale(0.7f * uiScale);
    description.setWrap(true);

    Label.LabelStyle priceStyle = new Label.LabelStyle(white);
    priceStyle.fontColor = GOLD_TEXT_COLOR;
    Label quantity = new Label("Quantity: x" + selectedListing.getQuantity(), white);
    quantity.setFontScale(0.8f * uiScale);
    Label price = new Label("Price: " + selectedListing.getPrice() + "g", priceStyle);
    price.setFontScale(0.8f * uiScale);
    Table stats = new Table();
    stats.add(quantity).left().row();
    stats.add(price).left();

    TextButton buyBtn = createBuyButton(selectedListing, entity.getComponent(ShopComponent.class));
    buyBtn.getLabel().setFontScale(0.8f * uiScale);

    detailsTable.add(name).left().colspan(2).row();
    detailsTable.add(description).growX().left().colspan(2).padTop(gap).row();
    detailsTable.add(stats).left().expandX().padTop(gap);
    detailsTable.add(buyBtn).size(detailsBuyWidth, detailsBuyHeight).right().padTop(gap);
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
