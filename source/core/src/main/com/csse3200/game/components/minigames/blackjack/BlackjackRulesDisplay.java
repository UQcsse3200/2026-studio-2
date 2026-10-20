package com.csse3200.game.components.minigames.blackjack;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.ui.UIComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows an "i" button in the top-left corner of the Blackjack table that opens a parchment panel
 * listing the rules.
 *
 * <p>Fires {@link #RULES_OPENED} and {@link #RULES_CLOSED} on its entity so the game display can
 * lock its buttons while the rules are shown. Listens for {@link #TABLE_RAISED} to stay above the
 * table, since components are created in no fixed order.
 */
public class BlackjackRulesDisplay extends UIComponent {
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

  private static final float INFO_BUTTON_SIZE = 56f;
  private static final float SCREEN_PADDING = 16f;

  /** Width of the text column. The parchment adds its own edge padding around it. */
  private static final float TEXT_WIDTH = 420f;

  /** Clears the parchment's brown side strip, which stretches with the panel to about 45px. */
  private static final float SIDE_PADDING = 60f;

  private static final String HEADING_COLOUR = "6B3C14";
  private static final String TEXT_COLOUR = "4B301B";

  private final List<Texture> ownedTextures = new ArrayList<>();
  private Table corner;
  private Table rulesOverlay;
  private ImageButton infoButton;
  private ImageButton closeButton;
  private boolean open;

  @Override
  public void create() {
    super.create();
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

  private void buildRulesOverlay() {
    rulesOverlay = new Table();
    rulesOverlay.setFillParent(true);
    rulesOverlay.setVisible(false);
    rulesOverlay.setTouchable(Touchable.disabled);

    Label title = new Label("HOW TO PLAY", skin);
    title.setStyle(textStyle(title, HEADING_COLOUR));
    title.setFontScale(1.5f);
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

    Table rules = new Table();
    rules.setName("rulesList");
    addSection(rules, "THE MATCH", MATCH_RULES);
    addSection(rules, "THE CARDS", CARD_RULES);

    // No fixed width on the popup: the parchment sizes itself around the content, including the
    // padding its own edges add, so text can never spill onto the wooden rods.
    Table popup = new Table();
    popup.setBackground(BlackjackUiStyles.parchment(ownedTextures));
    popup.add(title).padTop(28f);
    popup.row();
    popup.add(rules).width(TEXT_WIDTH).padTop(8f).padLeft(SIDE_PADDING).padRight(SIDE_PADDING);
    popup.row();
    popup.add(closeButton).width(180f).height(60f).padTop(18f).padBottom(28f);

    rulesOverlay.add(popup);
    stage.addActor(rulesOverlay);
  }

  private void addSection(Table rules, String heading, List<String> lines) {
    Label headingLabel = new Label(heading, skin);
    headingLabel.setStyle(textStyle(headingLabel, HEADING_COLOUR));
    headingLabel.setFontScale(1.1f);
    rules.add(headingLabel).left().padTop(14f).padBottom(6f);
    rules.row();

    for (String line : lines) {
      Label lineLabel = new Label("-  " + line, skin);
      lineLabel.setStyle(textStyle(lineLabel, TEXT_COLOUR));
      lineLabel.setWrap(true);
      rules.add(lineLabel).width(TEXT_WIDTH).left().padBottom(5f);
      rules.row();
    }
  }

  private static Label.LabelStyle textStyle(Label label, String hexColor) {
    Label.LabelStyle style = new Label.LabelStyle(label.getStyle());
    style.fontColor = Color.valueOf(hexColor);
    return style;
  }

  /** Shows the rules panel above everything else on the table. */
  void openRules() {
    if (open) {
      return;
    }
    open = true;
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
    super.dispose();
  }
}
