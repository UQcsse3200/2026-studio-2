package com.csse3200.game.components.minigames.blackjack;

import com.csse3200.game.components.item.ItemType;

public final class BlackjackConfig {
  public static final String[] TEXTURES = createTextures();
  public static final ItemType WIN_REWARD = ItemType.ARROW;
  public static final int WIN_REWARD_QUANTITY = 1;

  private static String[] createTextures() {
    String[] textures = new String[56];
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

    textures[index++] = "images/minigames/blackjack/card_back.png";
    textures[index++] = "images/Buttons/back_up_btn.png";
    textures[index++] = "images/Buttons/back_down_btn.png";
    textures[index] = "images/minigames/blackjack/god_of_wind_background.png";

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

  private BlackjackConfig() {}
}
