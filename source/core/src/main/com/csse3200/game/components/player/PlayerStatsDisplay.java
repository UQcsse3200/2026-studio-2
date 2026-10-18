package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** A UI component that displays the player's health as a row of hearts, and their gold. */
public class PlayerStatsDisplay extends UIComponent {
  // Health bar
  private static final String HEART_TEXTURE = "images/health/red_heart.png";
  private static final String HEALTH_BAR_BACKGROUND_TEXTURE =
      "images/health/PixelArt_HeartBack.png";
  private static final String DAMAGED_HEART_TEXTURE = "images/health/Damaged_heart.png";
  private static final String LAST_HEALTH_TEXTURE = "images/health/Last_Health.png";
  private static final float HEALTH_BAR_WIDTH = 400f;
  private static final float HEALTH_BAR_HEIGHT = 150f;

  private static final float HEART_SIDE_LENGTH = 35f;
  private static final float HEART_SIDE_HEIGHT = 35f;
  private static final float HEART_SPACING = 43.4f;
  private static final int HP_PER_HEART = 2;
  private static final int FLICKER_COUNT = 3;
  private static final float FLICKER_DURATION = 0.1f;

  private static final float NORMAL_HEART_X = 124f;
  private static final float NORMAL_HEART_Y = 55f;
  private static final float DAMAGED_HEART_X = 123f;
  private static final float DAMAGED_HEART_Y = 56f;
  private static final float LAST_HEART_X = 124f;
  private static final float LAST_HEART_Y = 58f;

  private static final String HEALTH_LABEL_NAME = "player-health-label";
  private static final String SPEED_LABEL_NAME = "player-speed-label";

  // Gold bar
  private static final String GOLD_COIN_TEXTURE = "images/items/gold_coin.png";
  private static final String GOLD_BAR_BACKGROUND = "images/backgrounds/coin_bg.png";
  private static final float GOLD_BAR_WIDTH = 200f;
  private static final float GOLD_COIN_SIZE = 37f;
  private static final float GOLD_FONT_SCALE = 1f;
  private static final Color GOLD_TEXT_COLOR = new Color(246 / 255f, 194 / 255f, 88 / 255f, 1f);
  private static final Color GOLD_BAR_FILL_COLOR = new Color(0f, 0.25f, 0.28f, 1f);

  // Measurements of coin_bg.png (557x144), in pixels.
  // Centre of the coin ring, as a fraction of the image width (102 / 557 px).
  private static final float GOLD_RING_CENTER_X = 0.183f;
  // Left edge of the slot that holds the amount (right of the coin ring).
  private static final float GOLD_SLOT_LEFT = 164f;
  // Transparent interior of the bar (the ring and the slot).
  private static final float GOLD_INTERIOR_LEFT = 66f;
  private static final float GOLD_INTERIOR_RIGHT = 508f;
  private static final float GOLD_INTERIOR_TOP = 32f;
  private static final float GOLD_INTERIOR_BOTTOM = 110f;

  private Table root;
  private Group heartGroup;
  private final List<Image> heartImages = new ArrayList<>();
  private Texture heartTexture;
  private CombatStatsComponent combatStats;
  private PhysicsComponent physicsComponent;
  private Label healthLabel;
  private Label speedLabel;
  private Image background;
  private Texture goldBarFillTexture;
  private boolean showStatText;

  /**
   * Sets whether to show the health and speed text labels above the health bar.
   *
   * @param showStatText true to show the labels, false to hide them
   */
  public void setShowStatText(boolean showStatText) {
    this.showStatText = showStatText;
  }

  @Override
  public void create() {
    super.create();

    root = new Table();
    root.top().left();
    root.setFillParent(true);
    root.padTop(showStatText ? 5f : 45f);
    root.padLeft(5f);

    if (showStatText) {
      healthLabel = new Label("", skin);
      healthLabel.setName(HEALTH_LABEL_NAME);
      root.add(healthLabel).left().row();
    }

    root.add(createHealthBar()).size(HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT).left().row();

    if (showStatText) {
      speedLabel = new Label("Speed: 0.00", skin);
      speedLabel.setName(SPEED_LABEL_NAME);
      root.add(speedLabel).left();
    }

    root.add(createGoldBar()).left().padLeft(100f).padTop(-45f);

    stage.addActor(root);
  }

  /** Builds the heart bar: its background art with the hearts layered on top. */
  private Stack createHealthBar() {
    Stack stack = new Stack();
    Texture backgroundTexture =
        ServiceLocator.getResourceService().getAsset(HEALTH_BAR_BACKGROUND_TEXTURE, Texture.class);
    background = new Image(backgroundTexture);

    Table backgroundContainer = new Table();
    backgroundContainer.add(background).size(HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT).center();
    stack.add(backgroundContainer);

    heartTexture = ServiceLocator.getResourceService().getAsset(HEART_TEXTURE, Texture.class);
    heartGroup = new Group();
    heartGroup.setSize(HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
    stack.add(heartGroup);

    combatStats = entity.getComponent(CombatStatsComponent.class);
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    entity.getEvents().addListener("updateHealth", this::updatePlayerHealthUI);

    if (combatStats != null) {
      updatePlayerHealthUI(combatStats.getHealth());
    }
    return stack;
  }

  /**
   * Builds the gold bar: a fill behind the bar art, the art itself, then the coin in its ring and
   * the gold amount centred in its slot.
   */
  private Table createGoldBar() {
    Texture barTexture =
        ServiceLocator.getResourceService().getAsset(GOLD_BAR_BACKGROUND, Texture.class);
    float imageWidth = (float) barTexture.getWidth();
    float imageHeight = (float) barTexture.getHeight();
    float barWidth = GOLD_BAR_WIDTH;
    float barHeight = barWidth * imageHeight / imageWidth;

    Stack goldStack = new Stack();
    goldStack.add(createGoldBarFill(imageWidth, imageHeight, barWidth, barHeight));
    goldStack.add(new Image(barTexture));

    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
    if (inventory != null) {
      goldStack.add(createGoldContents(inventory, imageWidth, barWidth));
    }

    // Match the container to the image's aspect ratio so nothing spills outside the bar.
    Table goldContainer = new Table();
    goldContainer.add(goldStack).width(barWidth).height(barHeight);
    return goldContainer;
  }

  /**
   * Builds the coloured fill behind the bar art, limited to the bar's transparent interior so it
   * doesn't show through the outer corners.
   */
  private Table createGoldBarFill(
      float imageWidth, float imageHeight, float barWidth, float barHeight) {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(GOLD_BAR_FILL_COLOR);
    pixmap.fill();
    goldBarFillTexture = new Texture(pixmap);
    pixmap.dispose();

    Table fill = new Table();
    fill.setBackground(new TextureRegionDrawable(new TextureRegion(goldBarFillTexture)));

    Table layer = new Table();
    layer
        .add(fill)
        .grow()
        .padLeft(GOLD_INTERIOR_LEFT / imageWidth * barWidth)
        .padRight((imageWidth - GOLD_INTERIOR_RIGHT) / imageWidth * barWidth)
        .padTop(GOLD_INTERIOR_TOP / imageHeight * barHeight)
        .padBottom((imageHeight - GOLD_INTERIOR_BOTTOM) / imageHeight * barHeight);
    return layer;
  }

  /** Builds the coin and the gold amount, positioned over the ring and the slot of the bar art. */
  private Table createGoldContents(InventoryComponent inventory, float imageWidth, float barWidth) {
    Label.LabelStyle labelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
    labelStyle.fontColor = GOLD_TEXT_COLOR;
    Label goldLabel = new Label("" + inventory.getGold(), labelStyle);
    goldLabel.setFontScale(GOLD_FONT_SCALE);
    goldLabel.setAlignment(Align.center);

    Table contents = new Table();

    // The coin sits in the ring at the left of the bar.
    float coinPadLeft = GOLD_RING_CENTER_X * barWidth - GOLD_COIN_SIZE / 2f;
    Texture coinTexture =
        ServiceLocator.getResourceService().getAsset(GOLD_COIN_TEXTURE, Texture.class);
    contents.add(new Image(coinTexture)).size(GOLD_COIN_SIZE).left().padLeft(coinPadLeft);

    // The amount gets a cell exactly the size of the slot, with its text centred inside.
    float slotLeft = GOLD_SLOT_LEFT / imageWidth * barWidth;
    float slotWidth = (GOLD_INTERIOR_RIGHT - GOLD_SLOT_LEFT) / imageWidth * barWidth;
    contents
        .add(goldLabel)
        .width(slotWidth)
        .left()
        .padLeft(slotLeft - (coinPadLeft + GOLD_COIN_SIZE));
    contents.add().expandX();

    entity
        .getEvents()
        .addListener("goldChanged", () -> goldLabel.setText("" + inventory.getGold()));
    return contents;
  }

  @Override
  public void draw(SpriteBatch batch) {
    if (speedLabel == null || physicsComponent == null || physicsComponent.getBody() == null) {
      return;
    }
    float horizontalSpeed = Math.abs(physicsComponent.getBody().getLinearVelocity().x);
    speedLabel.setText(String.format(Locale.ROOT, "Speed: %.2f", horizontalSpeed));
  }

  /**
   * Updates the health UI whenever the player's health changes.
   *
   * @param health current player health
   */
  public void updatePlayerHealthUI(int health) {
    if (combatStats == null) {
      return;
    }

    int maximumHearts = Math.max(1, (combatStats.getMaxHealth() + HP_PER_HEART - 1) / HP_PER_HEART);
    growHeartsTo(maximumHearts);

    int cappedHealth = Math.clamp(health, 0, combatStats.getMaxHealth());
    if (healthLabel != null) {
      healthLabel.setText("Health: " + cappedHealth + " / " + combatStats.getMaxHealth());
    }

    int heartsRemaining = (cappedHealth + HP_PER_HEART - 1) / HP_PER_HEART;
    for (int i = 0; i < heartImages.size(); i++) {
      Image heart = heartImages.get(i);
      boolean shouldBeVisible = i < heartsRemaining;

      if (heart.isVisible() && !shouldBeVisible) {
        flickerAndHide(heart);
      } else if (!heart.isVisible() && shouldBeVisible) {
        heart.clearActions();
        heart.setColor(1f, 1f, 1f, 1f);
        heart.setVisible(true);
      }
    }

    updateHealthBarBackground(heartsRemaining);
  }

  /**
   * Grows the number of heart images to the desired count, adding new heart images as needed.
   *
   * @param desiredCount the desired number of heart images
   */
  private void growHeartsTo(int desiredCount) {
    while (heartImages.size() < desiredCount) {
      int index = heartImages.size();
      Image heart = new Image(heartTexture);
      heart.setSize(HEART_SIDE_LENGTH, HEART_SIDE_HEIGHT);
      heart.setPosition(NORMAL_HEART_X + index * HEART_SPACING, NORMAL_HEART_Y);
      heartImages.add(heart);
      heartGroup.addActor(heart);
    }
  }

  /**
   * Updates the health bar background based on the number of hearts remaining.
   *
   * @param heartsRemaining the number of hearts remaining
   */
  private void updateHealthBarBackground(int heartsRemaining) {
    Texture newBackground;
    float startX;
    float startY;

    if (heartsRemaining <= 1) {
      newBackground =
          ServiceLocator.getResourceService().getAsset(LAST_HEALTH_TEXTURE, Texture.class);
      startX = LAST_HEART_X;
      startY = LAST_HEART_Y;
    } else if (heartsRemaining <= 3) {
      newBackground =
          ServiceLocator.getResourceService().getAsset(DAMAGED_HEART_TEXTURE, Texture.class);
      startX = DAMAGED_HEART_X;
      startY = DAMAGED_HEART_Y;
    } else {
      newBackground =
          ServiceLocator.getResourceService()
              .getAsset(HEALTH_BAR_BACKGROUND_TEXTURE, Texture.class);
      startX = NORMAL_HEART_X;
      startY = NORMAL_HEART_Y;
    }

    background.setDrawable(new Image(newBackground).getDrawable());
    positionHearts(startX, startY);
  }

  /**
   * Positions the heart images in a row, starting from the specified coordinates.
   *
   * @param startX the x-coordinate to start positioning hearts
   * @param startY the y-coordinate to start positioning hearts
   */
  private void positionHearts(float startX, float startY) {
    for (int i = 0; i < heartImages.size(); i++) {
      Image heart = heartImages.get(i);
      heart.setPosition(startX + i * HEART_SPACING, startY);
    }
  }

  /**
   * Makes a heart flicker and then hides it. Used when the player loses health.
   *
   * @param heart the heart image to flicker and hide
   */
  private void flickerAndHide(Image heart) {
    heart.clearActions();
    heart.addAction(
        Actions.sequence(
            Actions.repeat(
                FLICKER_COUNT,
                Actions.sequence(
                    Actions.fadeOut(FLICKER_DURATION), Actions.fadeIn(FLICKER_DURATION))),
            Actions.run(
                () -> {
                  heart.setVisible(false);
                  heart.setColor(1f, 1f, 1f, 1f);
                })));
  }

  public void hide() {
    if (root != null) {
      root.setVisible(false);
    }
  }

  public void show() {
    if (root != null) {
      root.setVisible(true);
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    if (root != null) {
      root.remove();
    }
    heartImages.clear();
    if (goldBarFillTexture != null) {
      goldBarFillTexture.dispose();
      goldBarFillTexture = null;
    }
  }
}
