package com.csse3200.game.components.minigames.blackjack;

import com.csse3200.game.components.item.ItemType;
import java.util.List;

/**
 * Asset lists for the Blackjack minigame. Screens load {@link #TEXTURES} and {@link #SOUNDS} before
 * building the Blackjack UI. Background music is loaded separately by each screen.
 */
public final class BlackjackConfig {
  /** Every texture the Blackjack UI uses: all 52 card faces, the card back, buttons and table. */
  public static final List<String> TEXTURES = List.of(createTextures());

  /** Card deal, win and lose sound effects. */
  public static final List<String> SOUNDS =
      List.of(
          "sounds/minigames/blackjack/card-deal.mp3",
          "sounds/minigames/blackjack/win.mp3",
          "sounds/minigames/blackjack/lose.mp3");

  /** Item reward for winning a round. Not currently granted by the story-mode display. */
  public static final ItemType WIN_REWARD = ItemType.STANDARD_ARROW;

  /** Quantity of {@link #WIN_REWARD} for winning a round. */
  public static final int WIN_REWARD_QUANTITY = 1;

  private static String[] createTextures() {
    String[] textures = new String[64];
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
    textures[index++] = "images/Buttons/hit_up_btn.png";
    textures[index++] = "images/Buttons/hit_down_btn.png";
    textures[index++] = "images/Buttons/stand_up_btn.png";
    textures[index++] = "images/Buttons/stand_down_btn.png";
    textures[index++] = "images/Buttons/newRound_up_btn.png";
    textures[index++] = "images/Buttons/newRound_down_btn.png";
    textures[index++] = BlackjackRulesDisplay.INFO_UP_PATH;
    textures[index++] = BlackjackRulesDisplay.INFO_DOWN_PATH;
    textures[index] = "images/minigames/blackjack/blackjack_table_background.png";

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
