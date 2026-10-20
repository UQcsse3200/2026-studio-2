package com.csse3200.game.components.minigames.blackjack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.ui.UIComponent;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows an "i" button in the top-left corner of the Blackjack table that opens the rules on a
 * pixel-art scroll.
 *
 * <p>Fires {@link #RULES_OPENED} and {@link #RULES_CLOSED} on its entity so the game display can
 * lock its buttons while the rules are shown. Listens for {@link #TABLE_RAISED} to stay above the
 * table, since components are created in no fixed order.
 */
public class BlackjackRulesDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(BlackjackRulesDisplay.class);

  /** Fired when the rules panel opens. */
  public static final String RULES_OPENED = "blackjackRulesOpened";

  /** Fired when the rules panel closes. */
  public static final String RULES_CLOSED = "blackjackRulesClosed";

  /** Fired by the game display after it moves its table to the front. */
  public static final String TABLE_RAISED = "blackjackTableRaised";

  static final String INFO_UP_PATH = "images/Buttons/info_up_btn.png";
  static final String INFO_DOWN_PATH = "images/Buttons/info_down_btn.png";
  private static final String CLOSE_UP_PATH = "images/Buttons/back_up_btn.png";
  private static final String CLOSE_DOWN_PATH = "images/Buttons/back_down_btn.png";

  static final List<String> MATCH_RULES =
      List.of(
          "First to " + Blackjack.WINS_NEEDED + " round wins beats Circe.",
          "Each round costs " + Blackjack.BET + " gold.",
          "Win to get " + (Blackjack.BET * 2) + " gold back. A draw refunds your gold.",
          "Draws don't count towards the score.",
          "Run out of gold and you lose the match.");

  static final List<String> CARD_RULES =
      List.of(
          "Get closer to 21 than Circe without going over.",
          "Face cards are worth 10. Aces are 1 or 11.",
          "Circe draws until she reaches 17.");

  /** Bold pixel font the game's other text uses, closest to the Controls screen lettering. */
  private static final String RULES_FONT_PATH = "flat-earth/skin/fonts/PixeloidSans-Bold.fnt";

  /**
   * The scroll artwork, 159 x 79 pixels at its native size. It is only ever enlarged by a whole
   * number, so its pixels stay square and sharp.
   */
  static final String SCROLL_PATH = "images/ui/blackjack_scroll.png";

  private static final int SCROLL_WIDTH = 159;
  private static final int SCROLL_HEIGHT = 79;

  /** Parchment margins in artwork pixels: keeps text clear of the rods and the torn edges. */
  private static final int MARGIN_LEFT = 17;

  private static final int MARGIN_RIGHT = 17;
  private static final int MARGIN_TOP = 13;
  private static final int MARGIN_BOTTOM = 11;

  /** Share of the window the scroll may fill, so it never touches the screen edges. */
  private static final float MAX_WIDTH_SHARE = 0.95f;

  private static final float MAX_HEIGHT_SHARE = 0.92f;

  /**
   * Font scale for each artwork pixel of enlargement. Text is sized from the scroll's scale so it
   * grows and shrinks with the parchment and always fits on it.
   */
  private static final float FONT_PER_SCALE = 0.17f;

  private static final float TITLE_RATIO = 1.35f;
  private static final float HEADING_RATIO = 1.15f;

  private static final float INFO_BUTTON_SIZE = 56f;
  private static final float SCREEN_PADDING = 16f;

  /** Dark brown ink, sampled from the lettering on the Controls screen. */
  private static final String INK_COLOUR = "200A01";

  private final List<Texture> ownedTextures = new ArrayList<>();
  private BitmapFont rulesFont;
  private Table corner;
  private Table rulesOverlay;
  private int builtScale;
  private ImageButton infoButton;
  private ImageButton closeButton;
  private boolean open;

  @Override
  public void create() {
    super.create();
    rulesFont = loadRulesFont();
    buildInfoButton();
    buildRulesOverlay();
    entity.getEvents().addListener(TABLE_RAISED, this::raise);
  }

  private void buildInfoButton() {
    infoButton = BlackjackUiStyles.imageButton(INFO_UP_PATH, INFO_DOWN_PATH);
    infoButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClick();
            openRules();
          }
        });

    corner = new Table();
    corner.setFillParent(true);
    corner.top().left();
    corner.add(infoButton).size(INFO_BUTTON_SIZE).pad(SCREEN_PADDING);
    stage.addActor(corner);
  }

  /** Whole-number enlargement of the scroll that fits the current window. */
  private int scrollScale() {
    float byWidth = stage.getWidth() * MAX_WIDTH_SHARE / SCROLL_WIDTH;
    float byHeight = stage.getHeight() * MAX_HEIGHT_SHARE / SCROLL_HEIGHT;
    return Math.max(1, (int) Math.min(byWidth, byHeight));
  }

  /** Loads the scroll artwork, or returns the older generated parchment if it cannot load. */
  private Drawable scrollBackground() {
    try {
      Texture texture = new Texture(Gdx.files.internal(SCROLL_PATH));
      texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
      ownedTextures.add(texture);
      return new TextureRegionDrawable(texture);
    } catch (RuntimeException e) {
      logger.warn("Could not load scroll {}: {}", SCROLL_PATH, e.getMessage());
      return BlackjackUiStyles.parchment(ownedTextures);
    }
  }

  private void buildRulesOverlay() {
    int scale = scrollScale();
    builtScale = scale;
    float fontScale = FONT_PER_SCALE * scale;

    rulesOverlay = new Table();
    rulesOverlay.setFillParent(true);
    rulesOverlay.setVisible(false);
    rulesOverlay.setTouchable(Touchable.disabled);

    Label title = new Label("HOW TO PLAY", skin);
    title.setStyle(textStyle(title));
    title.setFontScale(fontScale * TITLE_RATIO);
    title.setAlignment(Align.center);

    closeButton = BlackjackUiStyles.imageButton(CLOSE_UP_PATH, CLOSE_DOWN_PATH);
    closeButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClick();
            closeRules();
          }
        });

    float columnWidth = (SCROLL_WIDTH - MARGIN_LEFT - MARGIN_RIGHT - 4) * scale / 2f;

    Table rules = new Table();
    rules.setName("rulesList");
    rules.top();
    Table matchColumn = section("THE MATCH", MATCH_RULES, columnWidth, fontScale, scale);
    Table cardsColumn = section("THE CARDS", CARD_RULES, columnWidth, fontScale, scale);
    rules.add(matchColumn).top().width(columnWidth);
    rules.add().width(4f * scale);
    rules.add(cardsColumn).top().width(columnWidth);

    // The popup is exactly the scroll's size; the margins keep the text on the parchment.
    Table popup = new Table();
    popup.setBackground(scrollBackground());
    popup.padTop(MARGIN_TOP * scale);
    popup.padLeft(MARGIN_LEFT * scale);
    popup.padRight(MARGIN_RIGHT * scale);
    popup.padBottom(MARGIN_BOTTOM * scale);
    popup.top();
    popup.add(title).padBottom(1.5f * scale);
    popup.row();
    popup.add(rules).expandY().top();
    popup.row();
    popup.add(closeButton).width(21f * scale).height(7f * scale);

    rulesOverlay
        .add(popup)
        .width((float) SCROLL_WIDTH * scale)
        .height((float) SCROLL_HEIGHT * scale);
    stage.addActor(rulesOverlay);
  }

  private Table section(
      String heading, List<String> lines, float width, float fontScale, int scale) {
    Table column = new Table();
    column.top().left();

    Label headingLabel = new Label(heading, skin);
    headingLabel.setStyle(textStyle(headingLabel));
    headingLabel.setFontScale(fontScale * HEADING_RATIO);
    column.add(headingLabel).left().padBottom(1.2f * scale);
    column.row();

    for (String line : lines) {
      Label lineLabel = new Label("-  " + line, skin);
      lineLabel.setStyle(textStyle(lineLabel));
      lineLabel.setFontScale(fontScale);
      lineLabel.setWrap(true);
      column.add(lineLabel).width(width).left().padBottom(0.6f * scale);
      column.row();
    }
    return column;
  }

  /** Rebuilds the panel when the window has changed enough to need a different scroll size. */
  private void rebuildIfResized() {
    if (scrollScale() == builtScale) {
      return;
    }
    rulesOverlay.remove();
    for (Texture texture : ownedTextures) {
      texture.dispose();
    }
    ownedTextures.clear();
    buildRulesOverlay();
  }

  /** Loads the rules font, or returns null so the skin's own font is used if it cannot load. */
  private static BitmapFont loadRulesFont() {
    try {
      BitmapFont font = new BitmapFont(Gdx.files.internal(RULES_FONT_PATH));
      font.getRegion()
          .getTexture()
          .setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      return font;
    } catch (RuntimeException e) {
      logger.warn("Could not load rules font {}: {}", RULES_FONT_PATH, e.getMessage());
      return null;
    }
  }

  private Label.LabelStyle textStyle(Label label) {
    Label.LabelStyle style = new Label.LabelStyle(label.getStyle());
    if (rulesFont != null) {
      style.font = rulesFont;
    }
    style.fontColor = Color.valueOf(INK_COLOUR);
    return style;
  }

  /** Shows the rules panel above everything else on the table. */
  void openRules() {
    if (open) {
      return;
    }
    open = true;
    rebuildIfResized();
    rulesOverlay.setVisible(true);
    rulesOverlay.setTouchable(Touchable.enabled);
    rulesOverlay.toFront();
    entity.getEvents().trigger(RULES_OPENED);
  }

  /** Hides the rules panel and hands control back to the table. */
  void closeRules() {
    if (!open) {
      return;
    }
    open = false;
    rulesOverlay.setVisible(false);
    rulesOverlay.setTouchable(Touchable.disabled);
    entity.getEvents().trigger(RULES_CLOSED);
  }

  /** Returns whether the rules panel is showing. */
  public boolean isOpen() {
    return open;
  }

  private void raise() {
    corner.toFront();
    if (open) {
      rulesOverlay.toFront();
    }
  }

  @Override
  public void draw(SpriteBatch batch) {
    // Scene2D actors on the stage render this UI.
  }

  @Override
  public void dispose() {
    if (corner != null) {
      corner.remove();
    }
    if (rulesOverlay != null) {
      rulesOverlay.remove();
    }
    for (Texture texture : ownedTextures) {
      texture.dispose();
    }
    ownedTextures.clear();
    if (rulesFont != null) {
      rulesFont.dispose();
      rulesFont = null;
    }
    super.dispose();
  }
}
