package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.files.SaveFile;
import com.csse3200.game.files.SaveManager;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The save-select screen's UI, styled after the main menu: the same shaking background, title logo
 * and image buttons, with three scroll-style rows listing the save slots. Clicking a filled row
 * loads it; each filled row also has a two-step delete button, and a Back button returns to the
 * menu.
 */
public class LoadSaveDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(LoadSaveDisplay.class);
  private static final float Z_INDEX = 2f;

  /** Row box colours, matching the game's parchment/wood UI motif. */
  private static final Color ROW_FILL = new Color(0.91f, 0.84f, 0.66f, 1f);

  private static final Color ROW_BORDER = new Color(0.35f, 0.18f, 0.08f, 1f);
  private static final Color TEXT_DARK = new Color(0.28f, 0.14f, 0.06f, 1f);
  private static final Color TEXT_MUTED = new Color(0.52f, 0.46f, 0.40f, 1f);
  private static final Color TEXT_COMPLETED = new Color(0.72f, 0.24f, 0.10f, 1f);
  private static final Color HINT_COLOR = new Color(0.83f, 0.48f, 0.23f, 1f);

  private Table table;
  private Table slotsTable;
  private Label hintLabel;
  private Drawable slotRowBackground;
  private Texture slotRowTexture;

  /** Slot whose delete button has been pressed once ("Sure?" state), or -1. */
  private int armedDeleteSlot = -1;

  @Override
  public void create() {
    super.create();
    addActors();
    entity.getEvents().addListener("SlotsChanged", this::refreshSlots);
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);
    float screenWidth = (float) Gdx.graphics.getWidth();
    float screenHeight = (float) Gdx.graphics.getHeight();
    float pad = screenHeight * 0.02f;

    Image background =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/backgrounds/main_menu_bg.png", Texture.class));

    // Oversized slightly so the shake below never reveals an edge/gap (same as the main menu).
    float overscan = 1.03f;
    float bgWidth = screenWidth * overscan;
    float bgHeight = screenHeight * overscan;
    background.setSize(bgWidth, bgHeight);
    background.setPosition(-(bgWidth - screenWidth) / 2f, -(bgHeight - screenHeight) / 2f);
    stage.addActor(background);

    float shakeAmount = 15f;
    float shakeDuration = 1f;
    background.addAction(
        Actions.forever(
            Actions.sequence(
                Actions.moveBy(shakeAmount, 0f, shakeDuration, Interpolation.sine),
                Actions.moveBy(-shakeAmount, shakeAmount, shakeDuration, Interpolation.sine),
                Actions.moveBy(-shakeAmount, -shakeAmount, shakeDuration, Interpolation.sine),
                Actions.moveBy(shakeAmount, 0f, shakeDuration, Interpolation.sine))));

    slotRowTexture = buildSlotRowTexture();
    slotRowBackground = new NinePatchDrawable(new NinePatch(slotRowTexture, 8, 8, 8, 8));

    Image title =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/ui/title_odysseus_logo.png", Texture.class));

    Label headerLabel = textLabel("SELECT A SAVE", Color.WHITE);
    headerLabel.setFontScale(1.4f);

    hintLabel = textLabel("", HINT_COLOR);
    hintLabel.setFontScale(0.9f);
    hintLabel.setVisible(false);

    slotsTable = new Table();

    Texture backUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/back_up_btn.png", Texture.class);
    Texture backDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/back_down_btn.png", Texture.class);
    ImageButton.ImageButtonStyle backButtonStyle = new ImageButton.ImageButtonStyle();
    backButtonStyle.up = new TextureRegionDrawable(backUpTexture);
    backButtonStyle.down = new TextureRegionDrawable(backDownTexture);
    ImageButton backButton = new ImageButton(backButtonStyle);
    backButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Back button clicked");
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("Back"));
          }
        });

    refreshSlots();

    float buttonWidth = screenWidth * 0.12f;
    float buttonHeight = screenHeight * 0.06f;

    table.add().expandY();
    table.row();
    table.add(title).width(screenWidth * 0.3f).height(screenHeight * 0.18f).padBottom(pad);
    table.row();
    table.add(headerLabel).padBottom(pad);
    table.row();
    table.add(hintLabel).padBottom(pad);
    table.row();
    table.add(slotsTable).center();
    table.row();
    table
        .add(backButton)
        .center()
        .width(buttonWidth)
        .height(buttonHeight)
        .padTop(pad * 2f)
        .padBottom(screenHeight * 0.05f);

    stage.addActor(table);
  }

  /** Resets the delete confirmation and rebuilds the slot rows from disk. */
  private void refreshSlots() {
    armedDeleteSlot = -1;
    rebuildSlotRows();
  }

  /** Rebuilds the slot rows from disk and updates the all-slots-full hint. */
  private void rebuildSlotRows() {
    slotsTable.clear();

    SaveFile[] saves;
    try {
      saves = SaveManager.listAll();
    } catch (RuntimeException e) {
      logger.warn("Could not read save slots: {}", e.getMessage());
      saves = new SaveFile[SaveManager.SLOT_COUNT];
    }

    float rowWidth = Gdx.graphics.getWidth() * 0.42f;
    float rowHeight = Gdx.graphics.getHeight() * 0.115f;
    for (int slot = 0; slot < saves.length; slot++) {
      slotsTable
          .add(buildSlotRow(slot, saves[slot]))
          .width(rowWidth)
          .height(rowHeight)
          .padTop(Gdx.graphics.getHeight() * 0.012f);
      slotsTable.row();
    }

    boolean allFull = true;
    for (SaveFile save : saves) {
      if (save == null) {
        allFull = false;
        break;
      }
    }
    hintLabel.setVisible(allFull);
    if (allFull) {
      hintLabel.setText("All slots full - delete a save to start a new game");
    }
  }

  /** Builds one slot's row: a clickable info area for loads, plus a delete button if filled. */
  private Table buildSlotRow(int slot, SaveFile save) {
    Table row = new Table();
    row.setBackground(slotRowBackground);
    float pad = Gdx.graphics.getWidth() * 0.012f;

    if (save == null) {
      Label empty = textLabel(String.format("Slot %d - Empty", slot + 1), TEXT_MUTED);
      row.add(empty).pad(pad).left().grow();
      return row;
    }

    Table info = new Table();
    Label titleLabel =
        textLabel(String.format("Slot %d - %s", slot + 1, levelName(save.currentLevel)), TEXT_DARK);
    info.add(titleLabel).left().growX().row();

    Label timeLabel =
        textLabel("Last played " + formatTimestamp(save.lastModifiedEpochMs), TEXT_MUTED);
    info.add(timeLabel).left().growX().row();

    if (save.player != null) {
      Label statsLabel =
          textLabel(
              String.format(
                  "HP %d/%d    %d gold",
                  save.player.health, save.player.maxHealth, save.player.gold),
              TEXT_DARK);
      info.add(statsLabel).left().growX().row();
    }

    if (save.completed) {
      Label completedLabel = textLabel("Completed", TEXT_COMPLETED);
      info.add(completedLabel).left().growX().row();
    }

    info.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            logger.debug("Save slot {} clicked", slot);
            ButtonSound.playClickThen(() -> entity.getEvents().trigger("Load", slot));
          }
        });

    TextButton.TextButtonStyle deleteStyle =
        new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
    deleteStyle.fontColor = TEXT_DARK;
    deleteStyle.overFontColor = TEXT_COMPLETED;
    deleteStyle.downFontColor = TEXT_DARK;
    TextButton deleteButton = new TextButton(armedDeleteSlot == slot ? "SURE?" : "X", deleteStyle);
    deleteButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            onDeleteClicked(slot);
          }
        });

    row.add(info).grow().pad(pad);
    row.add(deleteButton)
        .right()
        .pad(pad)
        .size(Gdx.graphics.getWidth() * 0.06f, Gdx.graphics.getHeight() * 0.05f);
    return row;
  }

  /**
   * A label using the shared pixel font and an explicit colour. The skin's default label style is
   * black, and libGDX multiplies the label's colour by the style's font colour, so colours must be
   * set through the style rather than {@link Label#setColor}.
   */
  private static Label textLabel(String text, Color color) {
    return new Label(text, new Label.LabelStyle(skin.getFont("font"), color));
  }

  /**
   * Two-step delete: the first press arms the button ("SURE?"), the second press deletes the slot
   * and lets {@link LoadSaveActions} refresh the list. Pressing a different slot's button disarms
   * the previous one instead.
   */
  private void onDeleteClicked(int slot) {
    if (armedDeleteSlot == slot) {
      armedDeleteSlot = -1;
      ButtonSound.playClick();
      entity.getEvents().trigger("Delete", slot);
    } else {
      armedDeleteSlot = slot;
      ButtonSound.playClick();
      rebuildSlotRows();
    }
  }

  /** level name for the save-select screen. */
  private static String levelName(String level) {
    switch (level) {
      case "level1":
        return "Level 1";
      case "level2":
        return "Level 2";
      case "level3":
        return "Level 3";
      case "boss":
        return "Boss Arena";
      default:
        return level == null ? "Unknown level" : level;
    }
  }

  private static String formatTimestamp(long millis) {
    if (millis <= 0) {
      return "an unknown time";
    }
    try {
      return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
          .withZone(ZoneId.systemDefault())
          .format(Instant.ofEpochMilli(millis));
    } catch (RuntimeException e) {
      return "an unknown time";
    }
  }

  /**
   * Builds the shared rounded parchment box used behind every slot row, sized so the corners and a
   * small stretchable middle survive any row size (same technique as the inventory slot boxes).
   */
  private static Texture buildSlotRowTexture() {
    int r = 8;
    int border = 2;
    int size = r * 2 + 2;

    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        boolean inCornerBox = (x < r || x >= size - r) && (y < r || y >= size - r);
        if (inCornerBox) {
          float cx = (float) (x < r ? r : size - r - 1);
          float cy = (float) (y < r ? r : size - r - 1);
          double dist = Math.hypot(x - cx, y - cy);
          if (dist > r) {
            pixmap.drawPixel(x, y, Color.rgba8888(0f, 0f, 0f, 0f));
          } else if (dist > r - border) {
            pixmap.drawPixel(x, y, Color.rgba8888(ROW_BORDER));
          } else {
            pixmap.drawPixel(x, y, Color.rgba8888(ROW_FILL));
          }
        } else {
          boolean onBorder = x < border || x >= size - border || y < border || y >= size - border;
          pixmap.drawPixel(x, y, Color.rgba8888(onBorder ? ROW_BORDER : ROW_FILL));
        }
      }
    }
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  @Override
  public void draw(SpriteBatch batch) {}

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  @Override
  public void dispose() {
    table.clear();
    if (slotRowTexture != null) {
      slotRowTexture.dispose();
      slotRowTexture = null;
    }
    super.dispose();
  }
}
