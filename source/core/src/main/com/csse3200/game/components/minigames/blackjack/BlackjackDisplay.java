package com.csse3200.game.components.minigames.blackjack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/** Displays the Blackjack game and its cards. */
public class BlackjackDisplay extends UIComponent {
  /** Fired on the entity when the pause button is pressed. The screen opens its pause menu. */
  public static final String PAUSE_REQUESTED = "blackjackPauseRequested";

  private static final String EMPTY_BALANCE_MESSAGE = "Your balance is 0!";
  private static final String MATCH_OVER_MESSAGE = "The match is over.";

  private static final String PLACE_BET = "READY";
  private static final String MATCH_OVER = "MATCH OVER";

  private static final float Z_INDEX = 2f;
  private static final float PREF_OUTER_TABLE_WIDTH = 265f;
  private static final float PREF_INFO_BOX_HEIGHT = 400f;
  private static final float CARD_WIDTH = 100f;
  private static final float CARD_HEIGHT = 145f;
  private static final float MAX_HAND_WIDTH = 700f;
  private static final float DEAL_DURATION = 0.3f;
  private static final float DEAL_GAP = 0.15f;
  private static final float VICTORY_DIALOGUE_SECONDS = 1.5f;
  private static final float VICTORY_FADE_SECONDS = 0.75f;
  private static final String CARD_PATH = "images/minigames/blackjack/";
  public static final String DIALOGUE_SCROLL_PATH =
      "images/minigames/blackjack/blackjack_vertical_scroll.png";
  private static final String CARD_BACK_PATH = "images/minigames/blackjack/card_back.png";
  private static final String BACKGROUND_PATH =
      "images/minigames/blackjack/blackjack_table_background.png";
  private static final String CARD_DEAL_SOUND = "sounds/minigames/blackjack/card-deal.mp3";
  private static final String WIN_SOUND = "sounds/minigames/blackjack/win.mp3";
  private static final String LOSE_SOUND = "sounds/minigames/blackjack/lose.mp3";

  private final Blackjack blackjack;
  private final InventoryComponent inventory;
  private boolean resultSoundPlayed;
  private boolean soundEnabled = true;
  private final java.util.function.Consumer<Boolean> soundToggleCallback;
  private final Runnable backCallback;
  private final Runnable storyWinCallback;

  // UI-only textures; released when the Blackjack screen closes.
  private final java.util.List<Texture> casinoUiTextures = new java.util.ArrayList<>();

  private Table table;
  private Image deckImage;
  private Table dealerCards;
  private Table playerCards;
  private Table resultOverlay;
  private Table winFadeOverlay;
  private boolean winTransitionScheduled;
  private Table resultPopup;
  private Cell<Table> resultPopupCell;
  private Cell<Label> resultTitleCell;
  private Cell<Label> resultMessageCell;
  private Cell<ImageButton> resultButtonCell;
  private Drawable regularResultBackground;
  private Drawable dialogueScrollBackground;
  private ImageButton newRoundButton;
  private ImageButton hitButton;
  private ImageButton standButton;
  private ImageButton backButton;
  private ImageButton soundButton;
  private ImageButton pauseButton;

  private Label dealerLabel;
  private Label playerLabel;
  private Label goldLabel;
  private Label betSizeLabel;
  private Label resultLabel;
  private Label statusLabel;
  private Label scoreLabel;
  private Label dialogueActionLabel;
  private boolean goldSettled;
  private boolean dealInProgress;
  private boolean revealDealerCard;
  private boolean rulesOpen;

  /**
   * Creates a display with no inventory, so rounds cannot be started.
   *
   * @param blackjack game being shown
   */
  public BlackjackDisplay(Blackjack blackjack) {
    this(blackjack, null, null, null, null);
  }

  /**
   * Creates a display that stakes and pays out gold from the given inventory.
   *
   * @param blackjack game being shown
   * @param inventory inventory holding the player's gold
   */
  public BlackjackDisplay(Blackjack blackjack, InventoryComponent inventory) {
    this(blackjack, inventory, null, null, null);
  }

  /**
   * Creates a display whose sound button also controls the screen's music.
   *
   * @param blackjack game being shown
   * @param inventory inventory holding the player's gold
   * @param soundToggleCallback called with the new state when the sound button is pressed
   */
  public BlackjackDisplay(
      Blackjack blackjack,
      InventoryComponent inventory,
      java.util.function.Consumer<Boolean> soundToggleCallback) {
    this(blackjack, inventory, soundToggleCallback, null, null);
  }

  /**
   * Creates a display with every option.
   *
   * @param blackjack game being shown
   * @param inventory inventory holding the player's gold, or null if unavailable
   * @param soundToggleCallback called with the new state when the sound button is pressed, or null
   * @param backCallback run when Back is pressed; when null, a "back" event is triggered instead
   */
  public BlackjackDisplay(
      Blackjack blackjack,
      InventoryComponent inventory,
      java.util.function.Consumer<Boolean> soundToggleCallback,
      Runnable backCallback) {
    this(blackjack, inventory, soundToggleCallback, backCallback, null);
  }

  /** Creates a display with an optional story continuation after Circe's victory dialogue. */
  public BlackjackDisplay(
      Blackjack blackjack,
      InventoryComponent inventory,
      java.util.function.Consumer<Boolean> soundToggleCallback,
      Runnable backCallback,
      Runnable storyWinCallback) {
    this.blackjack = blackjack;
    this.inventory = inventory;
    this.soundToggleCallback = soundToggleCallback;
    this.backCallback = backCallback;
    this.storyWinCallback = storyWinCallback;
  }

  @Override
  public void create() {
    super.create();
    entity.getEvents().addListener(BlackjackRulesDisplay.RULES_OPENED, () -> setRulesOpen(true));
    entity.getEvents().addListener(BlackjackRulesDisplay.RULES_CLOSED, () -> setRulesOpen(false));
    buildUI();
    refresh();
    // Components are created in no fixed order, so let the rules layer move back on top.
    entity.getEvents().trigger(BlackjackRulesDisplay.TABLE_RAISED);
  }

  private void setRulesOpen(boolean open) {
    rulesOpen = open;
    updateResultOverlay();
  }

  private void buildUI() {
    table = new Table();
    table.setFillParent(true);
    table.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);

    table.addListener(
        new com.badlogic.gdx.scenes.scene2d.InputListener() {
          @Override
          public boolean touchDown(
              com.badlogic.gdx.scenes.scene2d.InputEvent event,
              float x,
              float y,
              int pointer,
              int button) {
            return true;
          }
        });

    Texture backgroundTexture =
        ServiceLocator.getResourceService().getAsset(BACKGROUND_PATH, Texture.class);

    backgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    table.setBackground(new TextureRegionDrawable(backgroundTexture));

    dealerLabel = new Label("CERCI", skin);
    playerLabel = new Label("PLAYER", skin);

    // Give the player and dealer titles their own gold-themed styles.
    Label.LabelStyle dealerStyle = new Label.LabelStyle(dealerLabel.getStyle());
    dealerStyle.fontColor = Color.valueOf("F5D78B");
    dealerLabel.setStyle(dealerStyle);

    Label.LabelStyle playerStyle = new Label.LabelStyle(playerLabel.getStyle());
    playerStyle.fontColor = Color.valueOf("F5D78B");
    playerLabel.setStyle(playerStyle);

    dealerLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
    playerLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

    goldLabel = new Label("", skin);
    betSizeLabel = new Label("", skin);
    resultLabel = new Label("", skin);
    statusLabel = new Label(PLACE_BET, skin);
    scoreLabel = new Label("", skin);

    // Gold text directly on the felt: no navy panels behind the scoreboard or HUD.
    applyGoldInfoLabelStyle(dealerLabel, 1.40f);
    applyGoldInfoLabelStyle(playerLabel, 1.40f);
    applyGoldInfoLabelStyle(goldLabel, 1.0f);
    applyGoldInfoLabelStyle(betSizeLabel, 1.0f);
    applyGoldInfoLabelStyle(scoreLabel, 1.0f);
    applyGoldInfoLabelStyle(statusLabel, 1.0f);
    applyGoldInfoLabelStyle(resultLabel, 0.92f);

    dealerCards = new Table();
    playerCards = new Table();

    /* Buttons */

    newRoundButton =
        createImageButton(
            "images/Buttons/new_round_up_btn.png", "images/Buttons/new_round_down_btn.png");
    newRoundButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClick();
            tryStartRound();
          }
        });

    hitButton =
        createImageButton("images/Buttons/hit_up_btn.png", "images/Buttons/hit_down_btn.png");
    hitButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClick();

            if (blackjack.isRoundInProgress() && !blackjack.isRoundOver()) {
              int playerCardsBefore = blackjack.getPlayerHand().size();
              blackjack.hit();
              settleGoldIfRoundOver();
              playResultSoundIfNeeded();
              animateNewCards(playerCards, playerCardsBefore);
            }
          }
        });

    standButton =
        createImageButton("images/Buttons/stand_up_btn.png", "images/Buttons/stand_down_btn.png");
    standButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClick();

            if (blackjack.isRoundInProgress() && !blackjack.isRoundOver()) {
              int dealerCardsBefore = blackjack.getDealerHand().size();

              blackjack.stand();
              revealDealerCard = true;

              settleGoldIfRoundOver();
              playResultSoundIfNeeded();
              animateNewCards(dealerCards, dealerCardsBefore);
            }
          }
        });

    backButton =
        createImageButton(
            "images/Buttons/bj_back_up_btn.png", "images/Buttons/bj_back_down_btn.png");
    backButton.addListener(
        new com.badlogic.gdx.scenes.scene2d.InputListener() {
          @Override
          public boolean touchDown(
              com.badlogic.gdx.scenes.scene2d.InputEvent event,
              float x,
              float y,
              int pointer,
              int button) {

            event.stop();
            return true;
          }

          @Override
          public void touchUp(
              com.badlogic.gdx.scenes.scene2d.InputEvent event,
              float x,
              float y,
              int pointer,
              int button) {

            event.stop();
            event.cancel();

            ButtonSound.playClick();

            if (backCallback != null) {
              backCallback.run();
            } else {
              entity.getEvents().trigger("back");
            }
          }
        });

    soundButton =
        createCheckedImageButton(
            "images/Buttons/sound_on_up_btn.png",
            "images/Buttons/sound_on_down_btn.png",
            "images/Buttons/sound_off_up_btn.png",
            "images/Buttons/sound_off_down_btn.png");
    soundButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            soundEnabled = !soundEnabled;

            if (soundToggleCallback != null) {
              soundToggleCallback.accept(soundEnabled);
            }

            if (soundEnabled) {
              ButtonSound.playClick();
            }
          }
        });

    pauseButton =
        createImageButton(
            "images/Buttons/bj_pause_up_btn.png", "images/Buttons/bj_pause_down_btn.png");
    pauseButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClick();
            entity.getEvents().trigger(PAUSE_REQUESTED);
          }
        });

    /* User Interface */

    Table dealerRegion = new Table();
    dealerRegion.add(dealerCards).height(CARD_HEIGHT + 20f).padTop(40f);
    dealerRegion.row();
    dealerRegion.add(dealerLabel);

    Table centerRegion = new Table();
    Group deckGroup = new Group();
    deckGroup.setSize(CARD_WIDTH + 12f, CARD_HEIGHT + 8f);
    Image deckBack = createCardBackImage();
    Image deckMiddle = createCardBackImage();
    deckImage = createCardBackImage();
    deckBack.setSize(CARD_WIDTH, CARD_HEIGHT);
    deckMiddle.setSize(CARD_WIDTH, CARD_HEIGHT);
    deckImage.setSize(CARD_WIDTH, CARD_HEIGHT);
    deckBack.setPosition(0f, 6f);
    deckMiddle.setPosition(4f, 3f);
    deckImage.setPosition(8f, 0f);
    deckGroup.addActor(deckBack);
    deckGroup.addActor(deckMiddle);
    deckGroup.addActor(deckImage);
    centerRegion.add(deckGroup).size(CARD_WIDTH + 12f, CARD_HEIGHT + 8f);

    Table playerRegion = new Table();
    playerRegion.add(playerLabel);
    playerRegion.row();
    playerRegion.add(playerCards).height(CARD_HEIGHT + 20f).padBottom(8f);

    /* Left Column  */
    Table leftSide = new Table();

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fill();
    Texture pixTexture = new Texture(pixmap);
    pixmap.dispose();
    casinoUiTextures.add(pixTexture);

    Table infoBox = new Table();
    infoBox.setBackground(new TextureRegionDrawable(pixTexture).tint(Color.valueOf("#C69B43")));

    Table infoBoxContent = new Table();
    infoBoxContent.setBackground(
        new TextureRegionDrawable(pixTexture).tint(Color.valueOf("#2A1608")));

    infoBoxContent.add(statusLabel).center().pad(5f);
    infoBoxContent.row();

    Table userInfoArea = new Table();
    userInfoArea.add(scoreLabel).padTop(5f);
    userInfoArea.row();
    userInfoArea.add(goldLabel).padTop(5f);
    userInfoArea.row();
    userInfoArea.add(betSizeLabel).padTop(5f);
    infoBoxContent.add(userInfoArea).fillY().expandY().center();
    infoBoxContent.row();

    resultLabel.setWrap(true);
    infoBoxContent.add(resultLabel).fillX().expandX().center().padTop(10f).padBottom(12f);
    infoBoxContent.row();

    infoBox.add(infoBoxContent).expand().fill().center().pad(5f);
    leftSide.add(infoBox).expand().fill().center();

    leftSide.row();
    leftSide.add(newRoundButton).width(160f).height(60f).center().padTop(10f);

    /* Middle Column */
    Table middle = new Table();
    middle.add(dealerRegion).expandX().fillX().height(245f).top();
    middle.row();
    middle.add(centerRegion).expand().center();
    middle.row();
    middle.add(playerRegion).expandX().fillX().height(220f).bottom();

    Table middleBottom = new Table();
    middleBottom.add(hitButton).width(192f).height(67f).pad(5f);
    middleBottom.add(standButton).width(192f).height(67f).pad(5f);

    middle.row();
    middle.add(middleBottom).expandX().fillX();

    /* Right Column */
    Table rightSide = new Table();

    rightSide.row();
    rightSide.add(soundButton).width(192f).height(67f).pad(5f);
    // Pause sits directly below Sound, in the same column and at the same size.
    rightSide.row();
    rightSide.add(pauseButton).width(192f).height(67f).pad(5f).padTop(15f);
    rightSide.top().right();

    table.add(leftSide).prefHeight(PREF_INFO_BOX_HEIGHT).prefWidth(PREF_OUTER_TABLE_WIDTH).center();
    table.add(middle).fill().expand();
    table.add(rightSide).fill().expandY().prefWidth(PREF_OUTER_TABLE_WIDTH);

    table.setFillParent(true);
    table.validate();
    table.toFront();
    table.pad(7f);
    stage.addActor(table);
    buildResultOverlay();
  }

  private void applyGoldInfoLabelStyle(Label label, float scale) {
    Label.LabelStyle style = new Label.LabelStyle(label.getStyle());
    style.fontColor = Color.valueOf("F5D78B");
    style.background = null;
    label.setStyle(style);
    label.setFontScale(scale);
    label.setAlignment(com.badlogic.gdx.utils.Align.center);
  }

  private TextField.TextFieldStyle createCasinoBetFieldStyle() {
    TextField.TextFieldStyle style =
        new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
    style.fontColor = Color.valueOf("FFE5A0");
    style.messageFontColor = Color.valueOf("D4BA78");
    style.background = createBetFieldBackground("103D30", "C69B43");
    style.focusedBackground = createBetFieldBackground("174B39", "F8D978");
    style.disabledBackground = createBetFieldBackground("26372E", "887344");
    return style;
  }

  private NinePatchDrawable createBetFieldBackground(String fillHex, String borderHex) {
    Pixmap pixels = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
    pixels.setColor(Color.valueOf(borderHex));
    pixels.fill();
    pixels.setColor(Color.valueOf(fillHex));
    pixels.fillRectangle(2, 2, 20, 20);
    Texture texture = new Texture(pixels);
    pixels.dispose();
    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    casinoUiTextures.add(texture);
    return new NinePatchDrawable(new NinePatch(texture, 4, 4, 4, 4));
  }

  private void buildResultOverlay() {
    resultOverlay = new Table();
    resultOverlay.setFillParent(true);
    resultOverlay.setVisible(false);
    resultOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);

    Table popup = new Table();
    resultPopup = popup;
    regularResultBackground = BlackjackUiStyles.parchment(casinoUiTextures);
    popup.setBackground(regularResultBackground);

    Label title = new Label("", skin);
    title.setName("resultTitle");
    title.setStyle(makeCasinoTextStyle(title, "6B3C14"));
    title.setFontScale(1.65f);
    title.setAlignment(com.badlogic.gdx.utils.Align.center);

    Label message = new Label("", skin);
    message.setStyle(makeCasinoTextStyle(message, "4B301B"));
    message.setFontScale(1.06f);
    message.setName("resultMessage");
    message.setWrap(true);

    ImageButton nextRoundButton =
        createImageButton(
            "images/Buttons/new_round_up_btn.png", "images/Buttons/new_round_down_btn.png");
    nextRoundButton.setName("resultContinue");

    nextRoundButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClick();

            if (!blackjack.isRoundInProgress()) {
              BlackjackDialogue dialogue = BlackjackDialogue.forMatch(blackjack);
              if (dialogue != null) {
                dialogue.pressButton(blackjack, storyWinCallback);
                if (dialogue.getKind() == BlackjackDialogue.Kind.MATCH_WON) {
                  if (storyWinCallback != null) {
                    return;
                  }
                  // Standalone Blackjack has no story transition; allow another match.
                  blackjack.restartMatchKeepingGold();
                }
                if (inventory != null) {
                  inventory.setGold(blackjack.getBalance());
                }
                resetMatchPresentation();
              }
              resultOverlay.setVisible(false);
              resultOverlay.setTouchable(Touchable.disabled);
              resultLabel.setText("");
              statusLabel.setText(
                  inventory != null && inventory.getGold() <= 0
                      ? EMPTY_BALANCE_MESSAGE
                      : PLACE_BET);
              refreshLabels();
              // Deal the next round now, so the player does not have to press New Round again.
              tryStartRound();
            }
          }
        });

    resultTitleCell = popup.add(title).padTop(35f).padLeft(40f).padRight(40f);
    popup.row();
    resultMessageCell = popup.add(message).width(360f).padTop(15f).padLeft(40f).padRight(40f);
    popup.row();
    dialogueActionLabel = new Label("", skin);
    dialogueActionLabel.setStyle(makeCasinoTextStyle(dialogueActionLabel, "6B3C14"));
    dialogueActionLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
    popup.add(dialogueActionLabel).padTop(14f);
    popup.row();
    resultButtonCell = popup.add(nextRoundButton).width(180f).height(60f).padTop(8f).padBottom(35f);

    resultPopupCell = resultOverlay.add(popup).width(480f);
    stage.addActor(resultOverlay);
  }

  /** Shows Cerci's final dialogue briefly, then fades to black before Cutscene 5. */
  private void scheduleVictoryCutscene() {
    if (winTransitionScheduled || storyWinCallback == null) {
      return;
    }
    winTransitionScheduled = true;

    Pixmap pixels = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixels.setColor(Color.BLACK);
    pixels.fill();
    Texture blackTexture = new Texture(pixels);
    pixels.dispose();
    casinoUiTextures.add(blackTexture);

    winFadeOverlay = new Table();
    winFadeOverlay.setFillParent(true);
    winFadeOverlay.setBackground(new TextureRegionDrawable(blackTexture));
    winFadeOverlay.getColor().a = 0f;
    winFadeOverlay.setTouchable(Touchable.enabled);
    stage.addActor(winFadeOverlay);
    winFadeOverlay.toFront();
    winFadeOverlay.addAction(
        Actions.sequence(
            Actions.delay(VICTORY_DIALOGUE_SECONDS),
            Actions.fadeIn(VICTORY_FADE_SECONDS),
            // Change screens on the next frame, not while Stage is acting its children.
            Actions.run(() -> Gdx.app.postRunnable(storyWinCallback))));
  }

  private Label.LabelStyle makeCasinoTextStyle(Label label, String hexColor) {
    Label.LabelStyle style = new Label.LabelStyle(label.getStyle());
    style.fontColor = Color.valueOf(hexColor);
    return style;
  }

  private void updateResultOverlay() {
    if (resultOverlay == null) {
      return;
    }

    boolean showResult = blackjack.isRoundOver();
    // While the rules are open the result waits, so it never covers the rules panel.
    boolean showOverlay = showResult && !dealInProgress && !rulesOpen;
    if (showOverlay && !resultOverlay.isVisible()) {
      resultOverlay.getColor().a = 0f;
      resultOverlay.setVisible(true);
      resultOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
      resultOverlay.addAction(Actions.fadeIn(0.25f));
    } else if (!showOverlay) {
      resultOverlay.setVisible(false);
      resultOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
      resultOverlay.getColor().a = 1f;
    }

    if (hitButton != null) {
      boolean disabled =
          !blackjack.isRoundInProgress() || showResult || dealInProgress || rulesOpen;
      hitButton.setDisabled(disabled);
      hitButton.setTouchable(
          disabled
              ? com.badlogic.gdx.scenes.scene2d.Touchable.disabled
              : com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    if (newRoundButton != null) {
      boolean disabled =
          blackjack.isRoundInProgress() || dealInProgress || blackjack.isMatchOver() || rulesOpen;
      newRoundButton.setDisabled(disabled);
      newRoundButton.setTouchable(
          disabled
              ? com.badlogic.gdx.scenes.scene2d.Touchable.disabled
              : com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    if (standButton != null) {
      boolean disabled =
          !blackjack.isRoundInProgress() || showResult || dealInProgress || rulesOpen;
      standButton.setDisabled(disabled);
      standButton.setTouchable(
          disabled
              ? com.badlogic.gdx.scenes.scene2d.Touchable.disabled
              : com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    if (backButton != null) {
      backButton.setDisabled(false);
      backButton.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    if (soundButton != null) {
      soundButton.setDisabled(false);
      soundButton.setTouchable(Touchable.enabled);
    }

    if (!showOverlay) {
      return;
    }

    Label title = resultOverlay.findActor("resultTitle");
    Label message = resultOverlay.findActor("resultMessage");

    BlackjackDialogue dialogue = BlackjackDialogue.forMatch(blackjack);
    setDialogueScrollStyle(true);
    boolean automaticVictory =
        dialogue != null
            && dialogue.getKind() == BlackjackDialogue.Kind.MATCH_WON
            && storyWinCallback != null;
    if (dialogue != null) {
      title.setText(BlackjackDialogue.SPEAKER);
      message.setText(dialogue.getText());
      dialogueActionLabel.setText(automaticVictory ? "" : dialogue.getButtonText());
    } else {
      title.setText(roundTitle(blackjack.getLastOutcome()));
      message.setText(blackjack.getResultMessage() + "\n" + scoreText());
      dialogueActionLabel.setText("Next round");
    }
    resultButtonCell.getActor().setVisible(!automaticVictory);
    resultOverlay.toFront();
    if (automaticVictory) {
      scheduleVictoryCutscene();
    }
  }

  /** Uses a tall scroll for Cerci's match dialogue, retaining the original round-result panel. */
  private void setDialogueScrollStyle(boolean dialogueVisible) {
    if (resultPopup == null || resultPopupCell == null) {
      return;
    }
    if (dialogueVisible) {
      if (dialogueScrollBackground == null) {
        Texture scroll =
            ServiceLocator.getResourceService().getAsset(DIALOGUE_SCROLL_PATH, Texture.class);
        scroll.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        dialogueScrollBackground = new TextureRegionDrawable(scroll);
      }
      resultPopup.setBackground(dialogueScrollBackground);
      resultPopupCell.width(480f).height(600f);
      resultMessageCell.width(340f).padLeft(35f).padRight(35f);
      resultTitleCell.padTop(105f);
      resultButtonCell.padBottom(100f);
    } else {
      resultPopup.setBackground(regularResultBackground);
      resultPopupCell.width(480f).height(Value.prefHeight);
      resultMessageCell.width(360f).padLeft(40f).padRight(40f);
      resultTitleCell.padTop(35f);
      resultButtonCell.padBottom(35f);
    }
    resultPopup.invalidateHierarchy();
  }

  private static String roundTitle(Blackjack.RoundOutcome outcome) {
    if (outcome == Blackjack.RoundOutcome.PLAYER_WIN) {
      return "YOU WIN!";
    }
    if (outcome == Blackjack.RoundOutcome.PUSH) {
      return "PUSH!";
    }
    return "YOU LOSE!";
  }

  private ImageButton createImageButton(String upPath, String downPath) {
    Texture upTexture = ServiceLocator.getResourceService().getAsset(upPath, Texture.class);
    Texture downTexture = ServiceLocator.getResourceService().getAsset(downPath, Texture.class);

    upTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    downTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    style.up = new TextureRegionDrawable(upTexture);
    style.down = new TextureRegionDrawable(downTexture);
    style.disabled = createDisabledDrawable(upTexture);

    return new ImageButton(style);
  }

  private ImageButton createCheckedImageButton(
      String upPathOn, String downPathOn, String upPathOff, String downPathOff) {
    ImageButton button = this.createImageButton(upPathOn, downPathOn);

    Texture upOffTexture = ServiceLocator.getResourceService().getAsset(upPathOff, Texture.class);
    upOffTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    Texture downOffTexture =
        ServiceLocator.getResourceService().getAsset(downPathOff, Texture.class);
    downOffTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    ImageButton.ImageButtonStyle style = button.getStyle();
    style.checked = new TextureRegionDrawable(upOffTexture);
    style.checkedDown = new TextureRegionDrawable(downOffTexture);

    button.setStyle(style);
    return button;
  }

  private SpriteDrawable createDisabledDrawable(Texture texture) {
    Sprite sprite = new Sprite(texture);
    sprite.setColor(0.45f, 0.45f, 0.45f, 0.75f);
    return new SpriteDrawable(sprite);
  }

  private String scoreText() {
    return "SCORE: " + blackjack.getPlayerWins() + " - " + blackjack.getDealerWins();
  }

  private void refresh() {
    renderHands();
    refreshLabels();
    updateResultOverlay();
  }

  private void renderHands() {
    dealerCards.clearChildren();
    playerCards.clearChildren();

    float dealerCardWidth = cardWidthForHand(blackjack.getDealerHand().size());
    float playerCardWidth = cardWidthForHand(blackjack.getPlayerHand().size());

    int dealerIndex = 0;

    for (Blackjack.Card card : blackjack.getDealerHand()) {
      Image cardImage;

      if (dealerIndex == 0 && blackjack.isRoundInProgress() && !blackjack.isRoundOver()) {
        cardImage = createCardBackImage();
      } else {
        cardImage = createCardImage(card);
      }

      dealerCards.add(cardImage).width(dealerCardWidth).height(CARD_HEIGHT).pad(5f);

      dealerIndex++;
    }

    for (Blackjack.Card card : blackjack.getPlayerHand()) {
      playerCards.add(createCardImage(card)).width(playerCardWidth).height(CARD_HEIGHT).pad(5f);
    }

    refreshTotalsAndResult();
  }

  /** Updates the hand totals, the gold and bet line, and the result message. */
  private void refreshTotalsAndResult() {
    if (blackjack.getDealerHand().isEmpty()) {
      dealerLabel.setText("CERCI");
    } else if (blackjack.isRoundInProgress() && !blackjack.isRoundOver()) {
      dealerLabel.setText("CERCI -- ??");
    } else {
      dealerLabel.setText("CERCI -- " + blackjack.getDealerTotal());
    }

    playerLabel.setText(
        blackjack.getPlayerHand().isEmpty() ? "PLAYER" : "PLAYER -- " + blackjack.getPlayerTotal());
    goldLabel.setText("GOLD:  " + getDisplayedGold());
    betSizeLabel.setText("STAKE:  " + blackjack.getBet());
    scoreLabel.setText(scoreText());

    if (inventory != null && inventory.getGold() <= 0) {
      resultLabel.setText(EMPTY_BALANCE_MESSAGE);
    } else {
      resultLabel.setText("");
    }
  }

  private void refreshLabels() {
    if (dealInProgress) {
      statusLabel.setText("DEALING");
    } else if (blackjack.isMatchOver()) {
      statusLabel.setText(MATCH_OVER);
    } else if (blackjack.isRoundOver()) {
      statusLabel.setText("ROUND OVER");
    } else if (blackjack.isRoundInProgress()) {
      statusLabel.setText("YOUR TURN");
    } else {
      statusLabel.setText(PLACE_BET);
    }

    refreshTotalsAndResult();
  }

  private void animateInitialDeal() {
    dealInProgress = true;
    renderHands();
    table.validate();
    refreshLabels();

    animateCards(playerCards, 0, 0, false);
    animateCards(dealerCards, 0, 1, false);
    animateCards(playerCards, 1, 2, false);
    animateCards(dealerCards, 1, 3, true);
    updateResultOverlay();
  }

  private void animateNewCards(Table hand, int index) {
    dealInProgress = true;
    renderHands();
    table.validate();
    refreshLabels();

    boolean revealingDealer = hand == dealerCards && revealDealerCard;
    if (revealingDealer) {
      Image revealedCard = (Image) dealerCards.getChildren().get(0);
      revealedCard.getColor().a = 0f;
    }

    int cardsToAnimate = hand.getChildren().size - index;
    if (cardsToAnimate <= 0) {
      finishDeal();
      return;
    }

    for (int cardIndex = index; cardIndex < hand.getChildren().size; cardIndex++) {
      boolean completesDeal = cardIndex == hand.getChildren().size - 1;
      animateCards(hand, cardIndex, cardIndex - index, completesDeal);
    }
    updateResultOverlay();
  }

  private float cardWidthForHand(int cardCount) {
    if (cardCount == 0) {
      return CARD_WIDTH;
    }
    return Math.min(CARD_WIDTH, MAX_HAND_WIDTH / cardCount);
  }

  private void animateCards(Table hand, int index, int sequenceIndex, boolean completesDeal) {
    if (hand.getChildren().size <= index) {
      if (completesDeal) {
        finishDeal();
      }
      return;
    }

    Image card = (Image) hand.getChildren().get(index);
    Vector2 target = new Vector2(card.getX(), card.getY());
    Vector2 deckPosition = deckImage.localToStageCoordinates(new Vector2(0f, 0f));
    Vector2 start = hand.stageToLocalCoordinates(deckPosition);
    card.setPosition(start.x, start.y);
    card.getColor().a = 0f;
    card.addAction(
        Actions.sequence(
            Actions.delay(sequenceIndex * (DEAL_DURATION + DEAL_GAP)),
            Actions.run(() -> playSound(CARD_DEAL_SOUND)),
            Actions.parallel(
                Actions.moveTo(target.x, target.y, DEAL_DURATION), Actions.fadeIn(DEAL_DURATION)),
            Actions.run(
                () -> {
                  if (completesDeal) {
                    finishDeal();
                  }
                })));
  }

  private void finishDeal() {
    if (revealDealerCard) {
      revealDealerCard = false;
      Image revealedCard = (Image) dealerCards.getChildren().get(0);
      revealedCard.addAction(
          Actions.sequence(Actions.fadeIn(DEAL_DURATION), Actions.run(this::finishDeal)));
      return;
    }
    dealInProgress = false;
    refreshLabels();
    updateResultOverlay();
  }

  private void playSound(String path) {
    if (!soundEnabled) {
      return;
    }

    try {
      if (ServiceLocator.getResourceService() == null) {
        return;
      }

      if (!ServiceLocator.getResourceService().containsAsset(path, Sound.class)) {
        return;
      }

      Sound sound = ServiceLocator.getResourceService().getAsset(path, Sound.class);

      sound.play(GameVolume.scale(1.0f));
    } catch (RuntimeException ignored) {
      // Ignore unavailable audio during tests or teardown.
    }
  }

  private void playResultSoundIfNeeded() {
    if (!blackjack.isRoundOver() || resultSoundPlayed) {
      return;
    }

    Blackjack.RoundOutcome outcome = blackjack.getLastOutcome();

    if (outcome == Blackjack.RoundOutcome.PLAYER_WIN) {
      playSound(WIN_SOUND);
      resultSoundPlayed = true;
    } else if (outcome == Blackjack.RoundOutcome.DEALER_WIN) {
      playSound(LOSE_SOUND);
      resultSoundPlayed = true;
    }
  }

  private void tryStartRound() {
    if (blackjack.isRoundInProgress()) {
      return;
    }

    if (inventory == null) {
      resultLabel.setText("Player gold unavailable.");
      return;
    }

    if (blackjack.isMatchOver()) {
      resultLabel.setText(MATCH_OVER_MESSAGE);
      statusLabel.setText(MATCH_OVER);
      return;
    }

    if (!inventory.hasGold(Blackjack.BET)) {
      refreshLabels();
      resultLabel.setText(EMPTY_BALANCE_MESSAGE);
      return;
    }

    blackjack.setBalance(inventory.getGold());
    inventory.addGold(-Blackjack.BET);

    goldSettled = false;
    resultSoundPlayed = false;
    revealDealerCard = false;

    blackjack.startNewRound();

    // New Round only starts the first deal; after that the result pop-up deals each round.
    if (newRoundButton != null) {
      newRoundButton.setVisible(false);
    }

    // A natural blackjack can end the round immediately after dealing.
    settleGoldIfRoundOver();
    playResultSoundIfNeeded();
    animateInitialDeal();
  }

  private void settleGoldIfRoundOver() {
    if (inventory == null || goldSettled || !blackjack.isRoundOver()) {
      return;
    }

    int bet = blackjack.getBet();
    Blackjack.RoundOutcome outcome = blackjack.getLastOutcome();

    if (outcome == Blackjack.RoundOutcome.PLAYER_WIN) {
      // Return the stake plus winnings: net profit = the original bet.
      inventory.addGold(bet * 2);
    } else if (outcome == Blackjack.RoundOutcome.PUSH) {
      // Push returns the original stake.
      inventory.addGold(bet);
    }

    // Keep the game's balance in step with the inventory for the end-of-round gold check.
    blackjack.setBalance(inventory.getGold());
    goldSettled = true;
  }

  /** Resets visual round state after a dialogue has applied its match action. */
  private void resetMatchPresentation() {
    goldSettled = false;
    resultSoundPlayed = false;
    revealDealerCard = false;
    dealInProgress = false;
    renderHands();
    refreshLabels();
  }

  private int getDisplayedGold() {
    return inventory == null ? blackjack.getBalance() : inventory.getGold();
  }

  private Image createCardBackImage() {
    Texture texture = ServiceLocator.getResourceService().getAsset(CARD_BACK_PATH, Texture.class);

    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    return new Image(texture);
  }

  private Image createCardImage(Blackjack.Card card) {
    String rankName;

    switch (card.getRank()) {
      case TWO:
        rankName = "2";
        break;
      case THREE:
        rankName = "3";
        break;
      case FOUR:
        rankName = "4";
        break;
      case FIVE:
        rankName = "5";
        break;
      case SIX:
        rankName = "6";
        break;
      case SEVEN:
        rankName = "7";
        break;
      case EIGHT:
        rankName = "8";
        break;
      case NINE:
        rankName = "9";
        break;
      case TEN:
        rankName = "10";
        break;
      case JACK:
        rankName = "jack";
        break;
      case QUEEN:
        rankName = "queen";
        break;
      case KING:
        rankName = "king";
        break;
      case ACE:
        rankName = "ace";
        break;
      default:
        throw new IllegalStateException("Unknown card rank");
    }

    String suitName = card.getSuit().name().toLowerCase();

    String texturePath = CARD_PATH + rankName + "_" + suitName + ".png";

    Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);

    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    return new Image(texture);
  }

  @Override
  public void draw(SpriteBatch batch) {
    // Scene2D actors on the stage render this UI.
  }

  /** Brings the Blackjack UI above the blurred backdrop. */
  public void toFront() {
    table.toFront();

    if (resultOverlay != null && resultOverlay.isVisible()) {
      resultOverlay.toFront();
    }
    entity.getEvents().trigger(BlackjackRulesDisplay.TABLE_RAISED);
    if (winFadeOverlay != null) {
      winFadeOverlay.toFront();
    }
  }

  /**
   * Returns whether the game can be paused right now. Deal animations are scene2d actions, so they
   * keep running while the game is paused, and finishing one raises the result pop-up above
   * everything, including the pause menu. The win fade also changes screen on its own.
   *
   * @return false while cards are being dealt or the win transition is running
   */
  public boolean canPause() {
    return !dealInProgress && !winTransitionScheduled;
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  @Override
  public void dispose() {

    stage.unfocusAll();

    if (table != null) {
      table.remove();
    }

    if (resultOverlay != null) {
      resultOverlay.remove();
    }
    if (winFadeOverlay != null) {
      winFadeOverlay.remove();
    }

    for (Texture casinoTexture : casinoUiTextures) {
      casinoTexture.dispose();
    }
    casinoUiTextures.clear();
    super.dispose();
  }
}
