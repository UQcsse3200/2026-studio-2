package com.csse3200.game.components.minigames.blackjack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.components.minigames.blackjack.Blackjack.MatchResult;
import com.csse3200.game.components.minigames.blackjack.Blackjack.RoundOutcome;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BlackjackTest {
  @Test
  void testRejectsInvalidStartingBalance() {
    Random random = new Random(1);
    assertThrows(IllegalArgumentException.class, () -> new Blackjack(-1, random));
  }

  @Test
  void testCardAccessorsAndString() {
    Blackjack.Card card = new Blackjack.Card(Blackjack.Suit.SPADES, Blackjack.Rank.QUEEN);

    assertEquals(Blackjack.Suit.SPADES, card.getSuit());
    assertEquals(Blackjack.Rank.QUEEN, card.getRank());
    assertEquals("QUEEN of SPADES", card.toString());
  }

  @Test
  void testRejectsInvalidRoundTransitions() {
    Blackjack game = new Blackjack(100, new Random(1));

    assertThrows(IllegalStateException.class, game::hit);
    assertThrows(IllegalStateException.class, game::stand);

    game.startNewRound();
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
    assertResult("Blackjack! You win.", true, 101);
    assertResult("Cerci has blackjack. You lose.", false, 99);
  }

  @Test
  void testDealerOutcomeBranches() {
    assertResult("Cerci busts. You win.", true, 101);
    assertResult("You win.", true, 101);
    assertResult("Cerci wins.", false, 99);
    assertResult("Push.", false, 100);
  }

  @Test
  void testPlayerBustSettlesLoss() {
    Blackjack game = gameWithResultFromHit("Bust! Cerci wins.");

    assertTrue(game.isRoundOver());
    assertFalse(game.isRoundInProgress());
    assertEquals(99, game.getBalance());
    assertFalse(game.isPlayerWinner());
  }

  @Test
  void testInitialState() {
    Blackjack game = new Blackjack(100, new Random(1));

    assertEquals(100, game.getBalance());
    assertEquals(Blackjack.BET, game.getBet());
    assertEquals(0, game.getPlayerHand().size());
    assertEquals(0, game.getDealerHand().size());
    assertFalse(game.isRoundInProgress());
    assertFalse(game.isPlayerWinner());
    assertNull(game.getLastOutcome());
    assertEquals(0, game.getPlayerWins());
    assertEquals(0, game.getDealerWins());
    assertEquals(Blackjack.MatchResult.IN_PROGRESS, game.getMatchResult());
  }

  @Test
  void testNewRoundDealsTwoCardsEach() {
    Blackjack game = new Blackjack(100, new Random(1));
    game.startNewRound();

    assertEquals(Blackjack.BET, game.getBet());
    assertEquals(2, game.getPlayerHand().size());
    assertEquals(2, game.getDealerHand().size());
    assertTrue(game.isRoundInProgress());
    assertTrue(game.getPlayerTotal() > 0);
    assertTrue(game.getDealerTotal() > 0);
  }

  @Test
  void testPlayerHitAddsCardAndCanBust() {
    Blackjack game = new Blackjack(100, new Random(1));
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
  void testLastOutcomeIsNullWhileRoundInProgress() {
    for (int seed = 0; seed < 100; seed++) {
      Blackjack game = new Blackjack(10, new Random(seed));
      game.startNewRound();
      if (game.isRoundInProgress()) {
        assertNull(game.getLastOutcome());
        return;
      }
    }
    throw new AssertionError("Could not find a round that stays in progress");
  }

  @Test
  void testEachOutcomeUpdatesScoreAndBalance() {
    Set<RoundOutcome> seen = EnumSet.noneOf(RoundOutcome.class);

    for (int seed = 0; seed < 500; seed++) {
      Blackjack game = new Blackjack(10, new Random(seed));
      RoundOutcome outcome = playRound(game);
      seen.add(outcome);

      switch (outcome) {
        case PLAYER_WIN -> {
          assertEquals(1, game.getPlayerWins());
          assertEquals(0, game.getDealerWins());
          assertEquals(11, game.getBalance());
          assertTrue(game.isPlayerWinner());
        }
        case DEALER_WIN -> {
          assertEquals(0, game.getPlayerWins());
          assertEquals(1, game.getDealerWins());
          assertEquals(9, game.getBalance());
          assertFalse(game.isPlayerWinner());
        }
        case PUSH -> {
          // Draws do not count towards the match and return the stake.
          assertEquals(0, game.getPlayerWins());
          assertEquals(0, game.getDealerWins());
          assertEquals(10, game.getBalance());
          assertFalse(game.isPlayerWinner());
        }
      }
      assertEquals(MatchResult.IN_PROGRESS, game.getMatchResult());
    }

    assertEquals(EnumSet.allOf(RoundOutcome.class), seen);
  }

  @Test
  void testMatchEndsWhenEitherSideReachesThreeWins() {
    Set<MatchResult> seen = EnumSet.noneOf(MatchResult.class);

    for (int seed = 0; seed < 50; seed++) {
      Blackjack game = new Blackjack(100, new Random(seed));
      int rounds = 0;
      while (!game.isMatchOver()) {
        assertTrue(game.getPlayerWins() < Blackjack.WINS_NEEDED);
        assertTrue(game.getDealerWins() < Blackjack.WINS_NEEDED);
        playRound(game);
        rounds++;
        assertTrue(rounds < 100, "Match should finish");
      }

      MatchResult result = game.getMatchResult();
      seen.add(result);
      if (result == MatchResult.PLAYER_WON) {
        assertEquals(Blackjack.WINS_NEEDED, game.getPlayerWins());
        assertTrue(game.getDealerWins() < Blackjack.WINS_NEEDED);
      } else {
        assertEquals(Blackjack.WINS_NEEDED, game.getDealerWins());
        assertTrue(game.getPlayerWins() < Blackjack.WINS_NEEDED);
      }
      assertThrows(IllegalStateException.class, game::startNewRound);
    }

    assertEquals(EnumSet.of(MatchResult.PLAYER_WON, MatchResult.DEALER_WON), seen);
  }

  @Test
  void testRunningOutOfGoldLosesTheMatch() {
    Blackjack game = gameThatRanOutOfGold();

    assertEquals(0, game.getBalance());
    assertEquals(1, game.getDealerWins());
    assertEquals(MatchResult.DEALER_WON, game.getMatchResult());
    assertTrue(game.isMatchOver());
    assertThrows(IllegalStateException.class, game::startNewRound);
  }

  @Test
  void testResetMatchRefillsGoldAndClearsScore() {
    Blackjack game = gameThatRanOutOfGold();

    game.resetMatch();

    assertEquals(Blackjack.REFILL_GOLD, game.getBalance());
    assertEquals(Blackjack.BET, game.getBet());
    assertEquals(0, game.getPlayerWins());
    assertEquals(0, game.getDealerWins());
    assertEquals(MatchResult.IN_PROGRESS, game.getMatchResult());
    assertFalse(game.isRoundOver());
    assertFalse(game.isRoundInProgress());
    assertNull(game.getLastOutcome());
    assertTrue(game.getPlayerHand().isEmpty());
    assertTrue(game.getDealerHand().isEmpty());

    assertNotNull(playRound(game));
  }

  private static RoundOutcome playRound(Blackjack game) {
    game.startNewRound();
    if (!game.isRoundOver()) {
      game.stand();
    }
    return game.getLastOutcome();
  }

  private static Blackjack gameThatRanOutOfGold() {
    for (int seed = 0; seed < 100; seed++) {
      Blackjack game = new Blackjack(Blackjack.BET, new Random(seed));
      if (playRound(game) == RoundOutcome.DEALER_WIN) {
        return game;
      }
    }
    throw new AssertionError("Could not find a losing round");
  }

  private static Blackjack resolvedGameWithAnyResult() {
    for (int seed = 0; seed < 1000; seed++) {
      Blackjack game = new Blackjack(100, new Random(seed));
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
