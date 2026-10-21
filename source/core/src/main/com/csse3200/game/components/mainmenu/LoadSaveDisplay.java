package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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

  @Override
  public void create() {
    super.create();
    addActors();
    entity.getEvents().addListener("SlotsChanged", this::refreshSlots);
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);
    int screenWidth = Gdx.graphics.getWidth();
    int screenHeight = Gdx.graphics.getHeight();
    float pad = screenHeight * 0.02f;

    stage.addActor(MenuBackground.create(screenWidth, screenHeight));

    slotRowTexture = buildSlotRowTexture();
    slotRowBackground = new NinePatchDrawable(new NinePatch(slotRowTexture, 8, 8, 8, 8));

    Image title =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/ui/title_odysseus_logo.png", Texture.class));

    Label headerLabel = textLabel("SELECT A SAVE", Color.BLACK);
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

    // The scroll art (the same background the game-end screen uses) sits behind the header, hint
    // and slot rows. Make it less wide and a bit longer as requested.
    Table panel = new Table();
    panel.setBackground(
        new TextureRegionDrawable(
            ServiceLocator.getResourceService()
                .getAsset("images/ui/scroll_bg.png", Texture.class)));
    float panelWidth = screenWidth * 0.52f;
    panel.add(headerLabel).padTop(pad * 1.4f).padBottom(pad * 0.6f).row();
    panel.add(hintLabel).padBottom(pad * 0.6f).row();
    panel.add(slotsTable).center().pad(pad * 0.4f).width(panelWidth).padBottom(pad * 1.2f);

    float buttonWidth = screenWidth * 0.12f;
    float buttonHeight = screenHeight * 0.06f;

    table.row();
    table
        .add(title)
        .width(screenWidth * 0.3f)
        .height(screenHeight * 0.18f)
        .padBottom(pad)
        .padTop(pad * 0.5f);
    table.row();
    table.add(panel).padBottom(pad).width(panelWidth);
    table.row();
    table
        .add(backButton)
        .center()
        .width(buttonWidth)
        .height(buttonHeight)
        .padTop(pad)
        .padBottom(screenHeight * 0.05f);

    stage.addActor(table);
  }

  /** Rebuilds the slot rows from disk. */
  private void refreshSlots() {
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

    ImageButton deleteButton = buildDeleteButton(slot);

    row.add(info).grow().pad(pad);
    row.add(deleteButton)
        .right()
        .pad(pad)
        .size(Gdx.graphics.getWidth() * 0.05f, Gdx.graphics.getHeight() * 0.045f);
    return row;
  }

  /** An X button that deletes the slot's save in one click (no confirmation step). */
  private ImageButton buildDeleteButton(int slot) {
    Texture upTexture =
        ServiceLocator.getResourceService().getAsset("images/Buttons/x_up_btn.png", Texture.class);
    Texture downTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/x_down_btn.png", Texture.class);
    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    style.up = new TextureRegionDrawable(upTexture);
    style.down = new TextureRegionDrawable(downTexture);
    ImageButton deleteButton = new ImageButton(style);
    deleteButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Deleting save slot {}", slot);
            ButtonSound.playClick();
            entity.getEvents().trigger("Delete", slot);
          }
        });
    return deleteButton;
  }

  /**
   * A label using the shared pixel font and an explicit colour. The skin's default label style is
   * black, and libGDX multiplies the label's colour by the style's font colour, so colours must be
   * set through the style rather than {@link Label#setColor}.
   */
  private static Label textLabel(String text, Color color) {
    return new Label(text, new Label.LabelStyle(skin.getFont("font"), color));
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
          int cx = x < r ? r : size - r - 1;
          int cy = y < r ? r : size - r - 1;
          double dist = Math.hypot((double) x - cx, (double) y - cy);
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
