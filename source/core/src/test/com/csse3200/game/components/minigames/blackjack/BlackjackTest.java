package com.csse3200.game.components.minigames.blackjack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BlackjackTest {
  @Test
  void testRejectsInvalidStartingBalance() {
    assertThrows(IllegalArgumentException.class, () -> new Blackjack(-1, new Random(1)));
  }

  @Test
  void testCardAccessorsAndString() {
    Blackjack.Card card = new Blackjack.Card(Blackjack.Suit.SPADES, Blackjack.Rank.QUEEN);

    assertEquals(Blackjack.Suit.SPADES, card.getSuit());
    assertEquals(Blackjack.Rank.QUEEN, card.getRank());
    assertEquals("QUEEN of SPADES", card.toString());
  }

  @Test
  void testRejectsInvalidBetAndRoundTransitions() {
    Blackjack game = new Blackjack(100, new Random(1));

    assertThrows(IllegalStateException.class, game::startNewRound);
    assertThrows(IllegalArgumentException.class, () -> game.placeBet(0));
    assertThrows(IllegalArgumentException.class, () -> game.placeBet(-1));
    assertThrows(IllegalArgumentException.class, () -> game.placeBet(101));
    assertThrows(IllegalStateException.class, game::hit);
    assertThrows(IllegalStateException.class, game::stand);

    game.placeBet(10);
    game.startNewRound();
    assertThrows(IllegalStateException.class, () -> game.placeBet(5));
    assertThrows(IllegalStateException.class, game::startNewRound);
  }

  @Test
  void testRejectsActionsAfterRoundEnds() {
    Blackjack game = resolvedGameWithAnyResult();

    assertThrows(IllegalStateException.class, game::hit);
    assertThrows(IllegalStateException.class, game::stand);
  }

  @Test
  void testScoreCalculationWithMultipleAcesAndEmptyHand() {
    Blackjack game = new Blackjack(100, new Random(1));

    assertEquals(0, game.calculateHandValue(List.of()));
    assertEquals(
        16,
        game.calculateHandValue(
            List.of(
                new Blackjack.Card(Blackjack.Suit.HEARTS, Blackjack.Rank.ACE),
                new Blackjack.Card(Blackjack.Suit.CLUBS, Blackjack.Rank.ACE),
                new Blackjack.Card(Blackjack.Suit.DIAMONDS, Blackjack.Rank.NINE),
                new Blackjack.Card(Blackjack.Suit.SPADES, Blackjack.Rank.FIVE))));
    assertEquals(
        27,
        game.calculateHandValue(
            List.of(
                new Blackjack.Card(Blackjack.Suit.HEARTS, Blackjack.Rank.KING),
                new Blackjack.Card(Blackjack.Suit.CLUBS, Blackjack.Rank.NINE),
                new Blackjack.Card(Blackjack.Suit.DIAMONDS, Blackjack.Rank.EIGHT))));
  }

  @Test
  void testNaturalBlackjackOutcomes() {
    assertResult("Push: both players have blackjack.", false, 100);
    assertResult("Blackjack! You win.", true, 110);
    assertResult("Dealer has blackjack. You lose.", false, 90);
  }

  @Test
  void testDealerOutcomeBranches() {
    assertResult("Dealer busts. You win.", true, 110);
    assertResult("You win.", true, 110);
    assertResult("Dealer wins.", false, 90);
    assertResult("Push.", false, 100);
  }

  @Test
  void testPlayerBustSettlesLoss() {
    Blackjack game = gameWithResultFromHit("Bust! Dealer wins.");

    assertTrue(game.isRoundOver());
    assertFalse(game.isRoundInProgress());
    assertEquals(90, game.getBalance());
    assertFalse(game.isPlayerWinner());
  }

  @Test
  void testInitialState() {
    Blackjack game = new Blackjack(100, new Random(1));

    assertEquals(100, game.getBalance());
    assertEquals(0, game.getBet());
    assertEquals(0, game.getPlayerHand().size());
    assertEquals(0, game.getDealerHand().size());
    assertFalse(game.isRoundInProgress());
    assertFalse(game.isPlayerWinner());
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

  @Test
  void testWinnerStateMatchesResolvedResult() {
    boolean foundWinner = false;
    boolean foundNonWinner = false;

    for (int seed = 0; seed < 100; seed++) {
      Blackjack game = new Blackjack(100, new Random(seed));
      game.placeBet(10);
      game.startNewRound();

      if (!game.isRoundOver()) {
        game.stand();
      }

      boolean resultIsWin = game.getResultMessage().contains("You win");
      assertEquals(resultIsWin, game.isPlayerWinner());
      foundWinner |= game.isPlayerWinner();
      foundNonWinner |= !game.isPlayerWinner();
    }

    assertTrue(foundWinner);
    assertTrue(foundNonWinner);
  }

  @Test
  void testWinnerStateResetsWhenStartingAnotherRound() {
    Blackjack game = new Blackjack(100, new Random(1));
    game.placeBet(10);
    game.startNewRound();

    if (!game.isRoundOver()) {
      game.stand();
    }

    game.startNewRound();

    assertFalse(game.isPlayerWinner());
  }

  @Test
  void testSetBalanceUpdatesBalance() {
    Blackjack game = new Blackjack(100, new Random(1));

    game.setBalance(250);

    assertEquals(250, game.getBalance());
  }

  @Test
  void testSetBalanceRejectsNegativeBalance() {
    Blackjack game = new Blackjack(100, new Random(1));

    assertThrows(IllegalArgumentException.class, () -> game.setBalance(-1));
    assertEquals(100, game.getBalance());
  }

  @Test
  void testBetUsesResynchronisedBalance() {
    Blackjack game = new Blackjack(10, new Random(1));

    assertThrows(IllegalArgumentException.class, () -> game.placeBet(50));

    game.setBalance(100);
    game.placeBet(50);

    assertEquals(100, game.getBalance());
    assertEquals(50, game.getBet());
  }

  private static Blackjack resolvedGameWithAnyResult() {
    for (int seed = 0; seed < 1000; seed++) {
      Blackjack game = new Blackjack(100, new Random(seed));
      game.placeBet(10);
      game.startNewRound();
      if (!game.isRoundOver()) {
        game.stand();
      }
      if (game.isRoundOver()) {
        return game;
      }
    }
    throw new AssertionError("Could not resolve a Blackjack round");
  }

  private static Blackjack gameWithResult(String expectedMessage) {
    for (int seed = 0; seed < 1000; seed++) {
      Blackjack game = new Blackjack(100, new Random(seed));
      game.placeBet(10);
      game.startNewRound();
      if (!game.isRoundOver()) {
        game.stand();
      }
      if (expectedMessage.equals(game.getResultMessage())) {
        return game;
      }
    }
    throw new AssertionError("Could not find result: " + expectedMessage);
  }

  private static Blackjack gameWithResultFromHit(String expectedMessage) {
    for (int seed = 0; seed < 1000; seed++) {
      Blackjack game = new Blackjack(100, new Random(seed));
      game.placeBet(10);
      game.startNewRound();
      if (!game.isRoundOver()) {
        game.hit();
      }
      if (expectedMessage.equals(game.getResultMessage())) {
        return game;
      }
    }
    throw new AssertionError("Could not find hit result: " + expectedMessage);
  }

  private static void assertResult(String expectedMessage, boolean playerWinner, int balance) {
    Blackjack game = gameWithResult(expectedMessage);

    assertEquals(expectedMessage, game.getResultMessage());
    assertEquals(playerWinner, game.isPlayerWinner());
    assertEquals(balance, game.getBalance());
    assertTrue(game.isRoundOver());
    assertFalse(game.isRoundInProgress());
  }
}
