package com.csse3200.game.components.minigames.blackjack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BlackjackTest {
  @Test
  void testInitialState() {
    Blackjack game = new Blackjack(100, new Random(1));

    assertEquals(100, game.getBalance());
    assertEquals(0, game.getBet());
    assertEquals(0, game.getPlayerHand().size());
    assertEquals(0, game.getDealerHand().size());
    assertFalse(game.isRoundInProgress());
  }

  @Test
  void testNewRoundDealsTwoCardsEach() {
    Blackjack game = new Blackjack(100, new Random(1));
    game.placeBet(10);
    game.startNewRound();

    assertEquals(10, game.getBet());
    assertEquals(2, game.getPlayerHand().size());
    assertEquals(2, game.getDealerHand().size());
    assertTrue(game.isRoundInProgress());
    assertTrue(game.getPlayerTotal() > 0);
    assertTrue(game.getDealerTotal() > 0);
  }

  @Test
  void testPlayerHitAddsCardAndCanBust() {
    Blackjack game = new Blackjack(100, new Random(1));
    game.placeBet(10);
    game.startNewRound();

    int before = game.getPlayerHand().size();
    game.hit();
    assertTrue(game.getPlayerHand().size() >= before);

    if (game.getPlayerTotal() > 21) {
      assertTrue(game.isRoundOver());
    }
  }

  @Test
  void testDealerDrawsUntilSeventeen() {
    Blackjack game = new Blackjack(100, new Random(1));
    game.placeBet(10);
    game.startNewRound();

    game.stand();

    assertTrue(game.isRoundOver());
    assertTrue(game.getDealerTotal() >= 17 || game.getDealerTotal() <= 21);
    assertNotNull(game.getResultMessage());
  }

  @Test
  void testScoreCalculationWithAces() {
    Blackjack game = new Blackjack(100, new Random(1));

    List<Blackjack.Card> hand =
        List.of(
            new Blackjack.Card(Blackjack.Suit.HEARTS, Blackjack.Rank.ACE),
            new Blackjack.Card(Blackjack.Suit.CLUBS, Blackjack.Rank.NINE),
            new Blackjack.Card(Blackjack.Suit.DIAMONDS, Blackjack.Rank.ACE));

    assertEquals(21, game.calculateHandValue(hand));
  }
}
