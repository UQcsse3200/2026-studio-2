package com.csse3200.game.components.minigames.blackjack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.extensions.GameExtension;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class BlackjackConfigTest {

  @Test
  void shouldContainEveryBlackjackTexture() {
    assertEquals(55, BlackjackConfig.TEXTURES.length);
    assertTrue(Arrays.stream(BlackjackConfig.TEXTURES).allMatch(path -> path != null && !path.isBlank()));

    for (String path : BlackjackConfig.TEXTURES) {
      assertTrue(Gdx.files.internal(path).exists(), path);
    }
  }

  @Test
  void shouldContainOneTextureForEveryCard() {
    List<String> paths = Arrays.asList(BlackjackConfig.TEXTURES);

    for (Blackjack.Suit suit : Blackjack.Suit.values()) {
      for (Blackjack.Rank rank : Blackjack.Rank.values()) {
        String rankName = rankFileName(rank);
        String path =
            "images/minigames/blackjack/"
                + rankName
                + "_"
                + suit.name().toLowerCase()
                + ".png";

        assertTrue(paths.contains(path), path);
      }
    }
  }

  @Test
  void shouldContainSharedBlackjackTextures() {
    assertNotNull(BlackjackConfig.TEXTURES);
    List<String> paths = Arrays.asList(BlackjackConfig.TEXTURES);

    assertTrue(paths.contains("images/minigames/blackjack/card_back.png"));
    assertTrue(paths.contains("images/Buttons/back_up_btn.png"));
    assertTrue(paths.contains("images/Buttons/back_down_btn.png"));
  }

  @Test
  void shouldConfigureAnInventoryReward() {
    InventoryComponent inventory = new InventoryComponent(0);

    assertTrue(BlackjackConfig.WIN_REWARD_QUANTITY > 0);
    assertTrue(
        inventory.addItem(
            BlackjackConfig.WIN_REWARD, BlackjackConfig.WIN_REWARD_QUANTITY));
    assertTrue(inventory.hasItem(BlackjackConfig.WIN_REWARD));
    assertEquals(
        BlackjackConfig.WIN_REWARD_QUANTITY,
      inventory.getItemCount(BlackjackConfig.WIN_REWARD));
  }

  private static String rankFileName(Blackjack.Rank rank) {
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
}
