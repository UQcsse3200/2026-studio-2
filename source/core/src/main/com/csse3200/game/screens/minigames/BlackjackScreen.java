package com.csse3200.game.screens.minigames;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.minigames.blackjack.Blackjack;
import com.csse3200.game.components.minigames.blackjack.BlackjackDisplay;
import com.csse3200.game.components.minigames.blackjack.BlackjackActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.services.ServiceLocator;

/** Screen for the Blackjack minigame. */
public class BlackjackScreen extends MinigameScreen {

  private static final String[] TEXTURES = createTextureList();

  public BlackjackScreen(GdxGame game) {
    super(game);
  }

  private static String[] createTextureList() {
    String[] textures = new String[53];
    int index = 0;

    for (Blackjack.Suit suit : Blackjack.Suit.values()) {
      for (Blackjack.Rank rank : Blackjack.Rank.values()) {
        textures[index++] =
            "images/minigames/blackjack/"
                + getRankFileName(rank)
                + "_"
                + suit.name().toLowerCase()
                + ".png";
      }
    }

    textures[index] = "images/minigames/blackjack/card_back.png";

    return textures;
  }

  private static String getRankFileName(Blackjack.Rank rank) {
    switch (rank) {
      case ACE:
        return "ace";
      case TWO:
        return "2";
      case THREE:
        return "3";
      case FOUR:
        return "4";
      case FIVE:
        return "5";
      case SIX:
        return "6";
      case SEVEN:
        return "7";
      case EIGHT:
        return "8";
      case NINE:
        return "9";
      case TEN:
        return "10";
      case JACK:
        return "jack";
      case QUEEN:
        return "queen";
      case KING:
        return "king";
      default:
        throw new IllegalArgumentException("Unknown rank: " + rank);
    }
  }

  @Override
  protected String[] getTextures() {
    return TEXTURES;
  }

  @Override
  protected Entity createUI() {
    Stage stage = ServiceLocator.getRenderService().getStage();

    Blackjack blackjack = new Blackjack(100);

    return new Entity()
        .addComponent(new BlackjackDisplay(blackjack))
        .addComponent(new BlackjackActions(game))
        .addComponent(new InputDecorator(stage, 10));
  }
}





