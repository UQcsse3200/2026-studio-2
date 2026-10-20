package com.csse3200.game.components.minigames.blackjack;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/** Displays the Blackjack game and its cards. */
public class BlackjackDisplay extends UIComponent {
  private static final String EMPTY_BALANCE_MESSAGE = "Your balance is 0 - grind hard!";
  private static final String MATCH_OVER_MESSAGE = "The match is over.";

  private static final String PLACE_BET = "READY";
  private static final String MATCH_OVER = "MATCH OVER";

  private static final float Z_INDEX = 2f;
  private static final float CARD_WIDTH = 100f;
  private static final float CARD_HEIGHT = 145f;
  private static final float MAX_HAND_WIDTH = 700f;
  private static final float DEAL_DURATION = 0.3f;
  private static final float DEAL_GAP = 0.15f;
  private static final String CARD_PATH = "images/minigames/blackjack/";
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

  // UI-only textures; released when the Blackjack screen closes.
  private final java.util.List<Texture> casinoUiTextures = new java.util.ArrayList<>();

  private Table table;
  private Image deckImage;
  private Table dealerCards;
  private Table playerCards;
  private Table resultOverlay;
  private ImageButton newRoundButton;
  private ImageButton hitButton;
  private ImageButton standButton;
  private ImageButton backButton;

  private Label dealerTotalLabel;
  private Label playerTotalLabel;
  private Label balanceLabel;
  private Label resultLabel;
  private Label statusLabel;
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
    this(blackjack, null, null, null);
  }

  /**
   * Creates a display that stakes and pays out gold from the given inventory.
   *
   * @param blackjack game being shown
   * @param inventory inventory holding the player's gold
   */
  public BlackjackDisplay(Blackjack blackjack, InventoryComponent inventory) {
    this(blackjack, inventory, null, null);
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
    this(blackjack, inventory, soundToggleCallback, null);
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
    this.blackjack = blackjack;
    this.inventory = inventory;
    this.soundToggleCallback = soundToggleCallback;
    this.backCallback = backCallback;
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

    Label dealerLabel = new Label("DEALER", skin);
    Label playerLabel = new Label("PLAYER", skin);

    // Give the player and dealer titles their own gold-themed styles.
    Label.LabelStyle dealerStyle = new Label.LabelStyle(dealerLabel.getStyle());
    dealerStyle.fontColor = Color.valueOf("F5D78B");
    dealerLabel.setStyle(dealerStyle);

    Label.LabelStyle playerStyle = new Label.LabelStyle(playerLabel.getStyle());
    playerStyle.fontColor = Color.valueOf("F5D78B");
    playerLabel.setStyle(playerStyle);

    dealerLabel.setFontScale(1.55f);
    playerLabel.setFontScale(1.55f);

    dealerLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
    playerLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

    dealerTotalLabel = new Label("", skin);
    playerTotalLabel = new Label("", skin);
    balanceLabel = new Label("", skin);
    resultLabel = new Label("", skin);
    statusLabel = new Label(PLACE_BET, skin);

    // Gold text directly on the felt: no navy panels behind the scoreboard or HUD.
    applyGoldInfoLabelStyle(dealerTotalLabel, 1.05f);
    applyGoldInfoLabelStyle(playerTotalLabel, 1.05f);
    applyGoldInfoLabelStyle(balanceLabel, 1.0f);
    applyGoldInfoLabelStyle(statusLabel, 1.0f);
    applyGoldInfoLabelStyle(resultLabel, 0.92f);

    dealerCards = new Table();
    playerCards = new Table();

    newRoundButton =
        BlackjackUiStyles.imageButton(
            "images/Buttons/newRound_up_btn.png", "images/Buttons/newRound_down_btn.png");
    hitButton =
        BlackjackUiStyles.imageButton(
            "images/Buttons/hit_up_btn.png", "images/Buttons/hit_down_btn.png");
    standButton =
        BlackjackUiStyles.imageButton(
            "images/Buttons/stand_up_btn.png", "images/Buttons/stand_down_btn.png");
    backButton =
        BlackjackUiStyles.imageButton(
            "images/Buttons/back_up_btn.png", "images/Buttons/back_down_btn.png");

    TextButton soundButton = new TextButton("SOUND: ON", skin);
    // Match the wooden texture used by the other Blackjack buttons.
    com.badlogic.gdx.graphics.Texture soundUpTexture =
        new com.badlogic.gdx.graphics.Texture(
            com.badlogic.gdx.Gdx.files.internal("images/Buttons/sound_up_btn.png"));
    com.badlogic.gdx.graphics.Texture soundDownTexture =
        new com.badlogic.gdx.graphics.Texture(
            com.badlogic.gdx.Gdx.files.internal("images/Buttons/sound_down_btn.png"));
    soundUpTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    soundDownTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    casinoUiTextures.add(soundUpTexture);
    casinoUiTextures.add(soundDownTexture);
    TextButton.TextButtonStyle soundStyle = new TextButton.TextButtonStyle(soundButton.getStyle());
    soundStyle.up = new TextureRegionDrawable(soundUpTexture);
    soundStyle.down = new TextureRegionDrawable(soundDownTexture);
    soundStyle.over = soundStyle.up;
    soundButton.setStyle(soundStyle);

    soundButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            soundEnabled = !soundEnabled;
            soundButton.setText(soundEnabled ? "SOUND: ON" : "SOUND: OFF");

            if (soundToggleCallback != null) {
              soundToggleCallback.accept(soundEnabled);
            }

            if (soundEnabled) {
              ButtonSound.playClick();
            }
          }
        });

    newRoundButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClick();
            tryStartRound();
          }
        });

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
    Table dealerRegion = new Table();
    dealerRegion.add(dealerLabel).padTop(45f).padBottom(-25f);
    dealerRegion.row();
    dealerRegion.add(dealerCards).height(CARD_HEIGHT + 20f).padTop(55f);
    dealerRegion.row();
    dealerRegion.add(dealerTotalLabel).padTop(4f);

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
    playerRegion.add(playerLabel).padBottom(4f);
    playerRegion.row();
    playerRegion.add(playerCards).height(CARD_HEIGHT + 20f).padBottom(8f);
    playerRegion.row();
    playerRegion.add(playerTotalLabel);

    // One compact status line above the buttons.
    Table status = new Table();
    status.add(statusLabel).padRight(22f);
    status.add(balanceLabel).padRight(22f);
    status.add(resultLabel).maxWidth(220f);

    Table buttons = new Table();

    buttons.add(newRoundButton).width(192f).height(67f).pad(5f);
    buttons.add(hitButton).width(192f).height(67f).pad(5f);
    buttons.add(standButton).width(192f).height(67f).pad(5f);
    buttons.add(backButton).width(192f).height(67f).pad(5f);
    buttons.add(soundButton).width(192f).height(67f).pad(5f);

    table.add(dealerRegion).expandX().fillX().height(245f).top();
    table.row();
    table.add(centerRegion).expand().center();
    table.row();
    table.add(playerRegion).expandX().fillX().height(220f).bottom();
    table.row();
    table.add(status).height(52f).center();
    table.row();
    table.add(buttons).height(85f).padBottom(12f).center();

    stage.addActor(table);

    table.pack();
    table.setFillParent(true);
    table.validate();
    table.toFront();
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

  private void buildResultOverlay() {
    resultOverlay = new Table();
    resultOverlay.setFillParent(true);
    resultOverlay.setVisible(false);
    resultOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);

    Table popup = new Table();
    popup.setBackground(BlackjackUiStyles.parchment(casinoUiTextures));

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
        BlackjackUiStyles.imageButton(
            "images/Buttons/newRound_up_btn.png", "images/Buttons/newRound_down_btn.png");

    nextRoundButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClick();

            if (!blackjack.isRoundInProgress()) {
              if (blackjack.getMatchResult() == Blackjack.MatchResult.DEALER_WON) {
                restartMatch();
              }
              resultOverlay.setVisible(false);
              resultOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
              resultLabel.setText("");
              statusLabel.setText(
                  inventory != null && inventory.getGold() <= 0
                      ? EMPTY_BALANCE_MESSAGE
                      : PLACE_BET);
              refreshLabels();
            }
          }
        });

    popup.add(title).padTop(35f).padLeft(40f).padRight(40f);
    popup.row();
    popup.add(message).width(360f).padTop(15f).padLeft(40f).padRight(40f);
    popup.row();
    popup.add(nextRoundButton).width(180f).height(60f).padTop(25f).padBottom(35f);

    resultOverlay.add(popup).width(480f);
    stage.addActor(resultOverlay);
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

    if (!showOverlay) {
      return;
    }

    Label title = resultOverlay.findActor("resultTitle");
    Label message = resultOverlay.findActor("resultMessage");

    switch (blackjack.getMatchResult()) {
      case PLAYER_WON -> title.setText("YOU WIN THE MATCH!");
      case DEALER_WON -> title.setText("YOU LOSE THE MATCH!");
      case IN_PROGRESS -> title.setText(roundTitle(blackjack.getLastOutcome()));
    }

    message.setText(blackjack.getResultMessage() + "\n" + scoreText());
    resultOverlay.toFront();
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

  private String scoreText() {
    return "SCORE  " + blackjack.getPlayerWins() + " - " + blackjack.getDealerWins();
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
      dealerTotalLabel.setText("TOTAL  --");
    } else if (blackjack.isRoundInProgress() && !blackjack.isRoundOver()) {
      dealerTotalLabel.setText("TOTAL  ??");
    } else {
      dealerTotalLabel.setText("TOTAL  " + blackjack.getDealerTotal());
    }

    playerTotalLabel.setText(
        blackjack.getPlayerHand().isEmpty() ? "TOTAL  --" : "TOTAL  " + blackjack.getPlayerTotal());
    balanceLabel.setText(
        "GOLD  " + getDisplayedGold() + "     STAKE  " + Blackjack.BET + "     " + scoreText());
    if (inventory != null && inventory.getGold() <= 0) {
      resultLabel.setText(EMPTY_BALANCE_MESSAGE);
    } else {
      resultLabel.setText(blackjack.getResultMessage());
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

  /** Starts a new match after a loss, refilling the player's gold. */
  private void restartMatch() {
    blackjack.resetMatch();
    if (inventory != null) {
      inventory.setGold(Blackjack.REFILL_GOLD);
    }
    goldSettled = false;
    resultSoundPlayed = false;
    revealDealerCard = false;
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

    for (Texture casinoTexture : casinoUiTextures) {
      casinoTexture.dispose();
    }
    casinoUiTextures.clear();
    super.dispose();
  }
}
