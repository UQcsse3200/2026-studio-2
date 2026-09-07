package com.csse3200.game.components.minigames.blackjack;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/** Displays the Blackjack game and its cards. */
public class BlackjackDisplay extends UIComponent {

  private static final float Z_INDEX = 2f;
  private static final float CARD_WIDTH = 100f;
  private static final float CARD_HEIGHT = 145f;
  private static final String CARD_PATH = "images/minigames/blackjack/";
  private static final String CARD_BACK_PATH = "images/minigames/blackjack/card_back.png";

  private final Blackjack blackjack;

  private Table table;
  private Table dealerCards;
  private Table playerCards;

  private Label dealerTotalLabel;
  private Label playerTotalLabel;
  private Label balanceLabel;
  private Label resultLabel;

  public BlackjackDisplay(Blackjack blackjack) {
    this.blackjack = blackjack;
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

    Label dealerLabel = new Label("Dealer", skin);
    Label playerLabel = new Label("Player", skin);

    dealerTotalLabel = new Label("", skin);
    playerTotalLabel = new Label("", skin);
    balanceLabel = new Label("", skin);
    resultLabel = new Label("", skin);

    dealerCards = new Table();
    playerCards = new Table();

    TextButton newRoundButton = new TextButton("New Round", skin);
    TextButton hitButton = new TextButton("Hit", skin);
    TextButton standButton = new TextButton("Stand", skin);
    TextButton backButton = new TextButton("Back", skin);

    newRoundButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (blackjack.getBet() == 0) {
              blackjack.placeBet(10);
            }

            if (!blackjack.isRoundInProgress()) {
              blackjack.startNewRound();
              refresh();
            }
          }
        });

    hitButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (blackjack.isRoundInProgress() && !blackjack.isRoundOver()) {
              blackjack.hit();
              refresh();
            }
          }
        });

    standButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (blackjack.isRoundInProgress() && !blackjack.isRoundOver()) {
              blackjack.stand();
              refresh();
            }
          }
        });

    backButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            entity.getEvents().trigger("back");
          }
        });

    table.top();

    table.add(dealerLabel).padTop(20f);
    table.row();

    table.add(dealerCards).padTop(10f);
    table.row();

    table.add(dealerTotalLabel).padTop(5f);
    table.row();

    table.add(playerLabel).padTop(30f);
    table.row();

    table.add(playerCards).padTop(10f);
    table.row();

    table.add(playerTotalLabel).padTop(5f);
    table.row();

    table.add(resultLabel).padTop(15f);
    table.row();

    table.add(balanceLabel).padTop(10f);
    table.row();

    Table buttons = new Table();

    buttons.add(newRoundButton).pad(5f);
    buttons.add(hitButton).pad(5f);
    buttons.add(standButton).pad(5f);
    buttons.add(backButton).pad(5f);

    table.add(buttons).padTop(15f);

    stage.addActor(table);
  }

  private void refresh() {
    dealerCards.clearChildren();
    playerCards.clearChildren();

    int dealerIndex = 0;

    for (Blackjack.Card card : blackjack.getDealerHand()) {
      Image cardImage;

      if (dealerIndex == 0 && blackjack.isRoundInProgress() && !blackjack.isRoundOver()) {
        cardImage = createCardBackImage();
      } else {
        cardImage = createCardImage(card);
      }

      dealerCards.add(cardImage).size(CARD_WIDTH, CARD_HEIGHT).pad(5f);

      dealerIndex++;
    }

    for (Blackjack.Card card : blackjack.getPlayerHand()) {
      playerCards.add(createCardImage(card)).size(CARD_WIDTH, CARD_HEIGHT).pad(5f);
    }

    dealerTotalLabel.setText(
        blackjack.getDealerHand().isEmpty()
            ? "Dealer total: -"
            : "Dealer total: " + blackjack.getDealerTotal());

    playerTotalLabel.setText(
        blackjack.getPlayerHand().isEmpty()
            ? "Player total: -"
            : "Player total: " + blackjack.getPlayerTotal());

    balanceLabel.setText("Balance: $" + blackjack.getBalance() + "    Bet: $" + blackjack.getBet());

    resultLabel.setText(blackjack.getResultMessage());
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

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}
