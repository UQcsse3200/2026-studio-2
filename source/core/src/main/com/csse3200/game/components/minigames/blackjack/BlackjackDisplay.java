package com.csse3200.game.components.minigames.blackjack;

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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/** Displays the Blackjack game and its cards. */
public class BlackjackDisplay extends UIComponent {

  private static final float Z_INDEX = 2f;
  private static final float CARD_WIDTH = 100f;
  private static final float CARD_HEIGHT = 145f;
  private static final float MAX_HAND_WIDTH = 700f;
  private static final float DEAL_DURATION = 0.3f;
  private static final float DEAL_GAP = 0.15f;
  private static final String CARD_PATH = "images/minigames/blackjack/";
  private static final String CARD_BACK_PATH = "images/minigames/blackjack/card_back.png";
  private static final String BACKGROUND_PATH =
      "images/minigames/blackjack/god_of_wind_background.png";
  private static final String CARD_DEAL_SOUND = "sounds/minigames/blackjack/card-deal.mp3";
  private static final String WIN_SOUND = "sounds/minigames/blackjack/win.mp3";
  private static final String LOSE_SOUND = "sounds/minigames/blackjack/lose.mp3";

  private final Blackjack blackjack;
  private final InventoryComponent inventory;
  private boolean rewardGranted;
  private boolean resultSoundPlayed;
  private boolean soundEnabled = true;
  private final java.util.function.Consumer<Boolean> soundToggleCallback;
  private final Runnable backCallback;

  private Table table;
  private Image deckImage;
  private Group deckGroup;
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
  private boolean dealInProgress;
  private boolean revealDealerCard;

  public BlackjackDisplay(Blackjack blackjack) {
    this(blackjack, null, null, null);
  }

  public BlackjackDisplay(Blackjack blackjack, InventoryComponent inventory) {
    this(blackjack, inventory, null, null);
  }

  public BlackjackDisplay(
      Blackjack blackjack,
      InventoryComponent inventory,
      java.util.function.Consumer<Boolean> soundToggleCallback) {
    this(blackjack, inventory, soundToggleCallback, null);
  }

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
    buildUI();
    refresh();
  }

  private void buildUI() {
    table = new Table();
    table.setFillParent(true);

    Texture backgroundTexture =
        ServiceLocator.getResourceService().getAsset(BACKGROUND_PATH, Texture.class);

    backgroundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    table.setBackground(new TextureRegionDrawable(backgroundTexture));

    Label dealerLabel = new Label("Dealer", skin);
    Label playerLabel = new Label("Player", skin);

    dealerTotalLabel = new Label("", skin);
    playerTotalLabel = new Label("", skin);
    balanceLabel = new Label("", skin);
    resultLabel = new Label("", skin);
    statusLabel = new Label("PLACE BET", skin);

    dealerCards = new Table();
    playerCards = new Table();

    newRoundButton =
        createImageButton(
            "images/Buttons/newRound_up_btn.png", "images/Buttons/newRound_down_btn.png");
    hitButton =
        createImageButton("images/Buttons/hit_up_btn.png", "images/Buttons/hit_down_btn.png");
    standButton =
        createImageButton("images/Buttons/stand_up_btn.png", "images/Buttons/stand_down_btn.png");
    backButton =
        createImageButton("images/Buttons/back_up_btn.png", "images/Buttons/back_down_btn.png");

    TextButton soundButton = new TextButton("SOUND: ON", skin);

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
            if (blackjack.getBet() == 0) {
              blackjack.placeBet(10);
            }

            ButtonSound.playClick();

            if (!blackjack.isRoundInProgress()) {
              rewardGranted = false;
              resultSoundPlayed = false;
              blackjack.startNewRound();
              playResultSoundIfNeeded();
              grantWinReward();
              animateInitialDeal();
            }
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
              playResultSoundIfNeeded();
              grantWinReward();
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

              playResultSoundIfNeeded();
              grantWinReward();
              animateNewCards(dealerCards, dealerCardsBefore);
            }
          }
        });

    backButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClick();

            if (backCallback != null) {
              backCallback.run();
            } else {
              entity.getEvents().trigger("back");
            }
          }
        });

    Table dealerRegion = new Table();
    dealerRegion.add(dealerLabel).padTop(12f);
    dealerRegion.row();
    dealerRegion.add(dealerCards).height(CARD_HEIGHT + 20f).padTop(8f);
    dealerRegion.row();
    dealerRegion.add(dealerTotalLabel).padTop(4f);

    Table centerRegion = new Table();
    deckGroup = new Group();
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

    Table status = new Table();
    status.add(statusLabel).padRight(30f);
    status.add(resultLabel).padRight(30f);
    status.add(balanceLabel);

    Table buttons = new Table();

    buttons.add(newRoundButton).width(160f).height(56f).pad(5f);
    buttons.add(hitButton).width(160f).height(56f).pad(5f);
    buttons.add(standButton).width(160f).height(56f).pad(5f);
    buttons.add(backButton).width(160f).height(56f).pad(5f);
    buttons.add(soundButton).width(140f).height(56f).pad(5f);

    table.add(dealerRegion).expandX().fillX().height(245f).top();
    table.row();
    table.add(centerRegion).expand().center();
    table.row();
    table.add(playerRegion).expandX().fillX().height(220f).bottom();
    table.row();
    table.add(status).height(35f).center();
    table.row();
    table.add(buttons).height(75f).padBottom(12f).center();

    stage.addActor(table);
    buildResultOverlay();
  }

  private void buildResultOverlay() {
    resultOverlay = new Table();
    resultOverlay.setFillParent(true);
    resultOverlay.setVisible(false);

    Table popup = new Table();
    popup.setBackground(createResultBackground());

    Label title = new Label("", skin);
    title.setName("resultTitle");

    Label message = new Label("", skin);
    message.setName("resultMessage");
    message.setWrap(true);

    ImageButton nextRoundButton =
        createImageButton(
            "images/Buttons/newRound_up_btn.png", "images/Buttons/newRound_down_btn.png");

    nextRoundButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            ButtonSound.playClick();

            if (!blackjack.isRoundInProgress()) {
              rewardGranted = false;
              resultSoundPlayed = false;
              blackjack.startNewRound();
              playSound(CARD_DEAL_SOUND);
              playResultSoundIfNeeded();
              grantWinReward();
              animateInitialDeal();
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

  private NinePatchDrawable createResultBackground() {
    int rodHeight = 24;
    int paperEdge = 16;
    int width = 80;
    int height = 72;

    Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

    Color parchment = new Color(0.82f, 0.68f, 0.43f, 1f);
    Color wood = new Color(0.35f, 0.18f, 0.08f, 1f);
    Color edge = new Color(0.55f, 0.38f, 0.20f, 1f);

    pixmap.setColor(parchment);
    pixmap.fillRectangle(paperEdge, 0, width - paperEdge * 2, height);

    pixmap.setColor(edge);
    pixmap.fillRectangle(paperEdge, 0, 4, height);
    pixmap.fillRectangle(width - paperEdge - 4, 0, 4, height);

    pixmap.setColor(wood);
    pixmap.fillRectangle(0, 0, width, rodHeight);
    pixmap.fillRectangle(0, height - rodHeight, width, rodHeight);

    int knobRadius = 8;
    pixmap.fillCircle(knobRadius, knobRadius, knobRadius);
    pixmap.fillCircle(width - knobRadius, knobRadius, knobRadius);
    pixmap.fillCircle(knobRadius, height - knobRadius, knobRadius);
    pixmap.fillCircle(width - knobRadius, height - knobRadius, knobRadius);

    Texture texture = new Texture(pixmap);
    pixmap.dispose();

    NinePatch patch = new NinePatch(texture, paperEdge, paperEdge, rodHeight, rodHeight);
    return new NinePatchDrawable(patch);
  }

  private void updateResultOverlay() {
    if (resultOverlay == null) {
      return;
    }

    boolean showResult = blackjack.isRoundOver();
    boolean showOverlay = showResult && !dealInProgress;
    if (showOverlay && !resultOverlay.isVisible()) {
      resultOverlay.getColor().a = 0f;
      resultOverlay.setVisible(true);
      resultOverlay.addAction(Actions.fadeIn(0.25f));
    } else if (!showOverlay) {
      resultOverlay.setVisible(false);
      resultOverlay.getColor().a = 1f;
    }

    if (hitButton != null) {
      boolean disabled = !blackjack.isRoundInProgress() || showResult || dealInProgress;
      hitButton.setDisabled(disabled);
      hitButton.setTouchable(
          disabled
              ? com.badlogic.gdx.scenes.scene2d.Touchable.disabled
              : com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    if (newRoundButton != null) {
      boolean disabled = blackjack.isRoundInProgress() || dealInProgress;
      newRoundButton.setDisabled(disabled);
      newRoundButton.setTouchable(
          disabled
              ? com.badlogic.gdx.scenes.scene2d.Touchable.disabled
              : com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    if (standButton != null) {
      boolean disabled = !blackjack.isRoundInProgress() || showResult || dealInProgress;
      standButton.setDisabled(disabled);
      standButton.setTouchable(
          disabled
              ? com.badlogic.gdx.scenes.scene2d.Touchable.disabled
              : com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    if (backButton != null) {
      boolean disabled = blackjack.isRoundInProgress() || dealInProgress;
      backButton.setDisabled(disabled);
      backButton.setTouchable(
          disabled
              ? com.badlogic.gdx.scenes.scene2d.Touchable.disabled
              : com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    if (!showResult || dealInProgress) {
      return;
    }

    Label title = resultOverlay.findActor("resultTitle");
    Label message = resultOverlay.findActor("resultMessage");

    if (blackjack.isPlayerWinner()) {
      title.setText("YOU WIN!");
    } else if (blackjack.getResultMessage() != null
        && blackjack.getResultMessage().startsWith("Push")) {
      title.setText("PUSH!");
    } else {
      title.setText("YOU LOSE!");
    }

    message.setText(blackjack.getResultMessage());
    resultOverlay.toFront();
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

  private SpriteDrawable createDisabledDrawable(Texture texture) {
    Sprite sprite = new Sprite(texture);
    sprite.setColor(0.45f, 0.45f, 0.45f, 0.75f);
    return new SpriteDrawable(sprite);
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

    if (blackjack.getDealerHand().isEmpty()) {
      dealerTotalLabel.setText("Dealer total: -");
    } else if (blackjack.isRoundInProgress() && !blackjack.isRoundOver()) {
      dealerTotalLabel.setText("Dealer total: ?");
    } else {
      dealerTotalLabel.setText("Dealer total: " + blackjack.getDealerTotal());
    }

    playerTotalLabel.setText(
        blackjack.getPlayerHand().isEmpty()
            ? "Player total: -"
            : "Player total: " + blackjack.getPlayerTotal());

    balanceLabel.setText("Balance: $" + blackjack.getBalance() + "    Bet: $" + blackjack.getBet());

    resultLabel.setText(blackjack.getResultMessage());
  }

  private void refreshLabels() {
    if (dealInProgress) {
      statusLabel.setText("DEALING");
    } else if (blackjack.isRoundOver()) {
      statusLabel.setText("ROUND OVER");
    } else if (blackjack.isRoundInProgress()) {
      statusLabel.setText("YOUR TURN");
    } else {
      statusLabel.setText("PLACE BET");
    }

    if (blackjack.getDealerHand().isEmpty()) {
      dealerTotalLabel.setText("Dealer total: -");
    } else if (blackjack.isRoundInProgress() && !blackjack.isRoundOver()) {
      dealerTotalLabel.setText("Dealer total: ?");
    } else {
      dealerTotalLabel.setText("Dealer total: " + blackjack.getDealerTotal());
    }

    playerTotalLabel.setText(
        blackjack.getPlayerHand().isEmpty()
            ? "Player total: -"
            : "Player total: " + blackjack.getPlayerTotal());
    balanceLabel.setText("Balance: $" + blackjack.getBalance() + "    Bet: $" + blackjack.getBet());
    resultLabel.setText(blackjack.getResultMessage());
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

      sound.play(1.0f);
    } catch (RuntimeException ignored) {
      // Ignore unavailable audio during tests or teardown.
    }
  }

  private void playResultSoundIfNeeded() {
    if (!blackjack.isRoundOver() || resultSoundPlayed) {
      return;
    }

    String result = blackjack.getResultMessage();

    if (blackjack.isPlayerWinner()) {
      playSound(WIN_SOUND);
      resultSoundPlayed = true;
    } else if (result != null && !result.isEmpty() && !result.startsWith("Push")) {
      playSound(LOSE_SOUND);
      resultSoundPlayed = true;
    }
  }

  private void grantWinReward() {
    if (inventory == null || rewardGranted || !blackjack.isPlayerWinner()) {
      return;
    }

    rewardGranted =
        inventory.addItem(BlackjackConfig.WIN_REWARD, BlackjackConfig.WIN_REWARD_QUANTITY);
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
  public void draw(SpriteBatch batch) {}

  /** Brings the Blackjack UI above the blurred backdrop. */
  public void toFront() {
    table.toFront();
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

    if (resultOverlay != null) {
      resultOverlay.remove();
    }

    super.dispose();
  }
}
