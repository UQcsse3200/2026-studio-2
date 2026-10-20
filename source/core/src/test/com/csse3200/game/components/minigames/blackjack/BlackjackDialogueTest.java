package com.csse3200.game.components.minigames.blackjack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.minigames.blackjack.Blackjack.MatchResult;
import com.csse3200.game.components.minigames.blackjack.Blackjack.RoundOutcome;
import com.csse3200.game.components.minigames.blackjack.BlackjackDialogue.Kind;
import com.csse3200.game.cutscene.CutsceneLoader;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class BlackjackDialogueTest {

  @Test
  void noDialogueWhileTheMatchIsStillGoing() {
    Blackjack game = new Blackjack(100, new Random(1));

    assertNull(BlackjackDialogue.forMatch(game));
  }

  @Test
  void winningTheMatchGivesTheWinDialogue() {
    Blackjack game = matchEndedWith(MatchResult.PLAYER_WON, 100);

    BlackjackDialogue dialogue = BlackjackDialogue.forMatch(game);

    assertEquals(Kind.MATCH_WON, dialogue.getKind());
    assertEquals("Continue", dialogue.getButtonText());
  }

  @Test
  void losingTheMatchWithGoldLeftGivesTheRetryDialogue() {
    Blackjack game = matchEndedWith(MatchResult.DEALER_WON, 100);
    assertTrue(game.getBalance() >= Blackjack.BET);

    BlackjackDialogue dialogue = BlackjackDialogue.forMatch(game);

    assertEquals(Kind.MATCH_LOST, dialogue.getKind());
    assertEquals("Retry", dialogue.getButtonText());
  }

  @Test
  void runningOutOfGoldGivesTheGoldDialogue() {
    Blackjack game = gameThatRanOutOfGold();

    BlackjackDialogue dialogue = BlackjackDialogue.forMatch(game);

    assertEquals(Kind.OUT_OF_GOLD, dialogue.getKind());
    assertEquals("Accept 10 gold", dialogue.getButtonText());
  }

  @Test
  void everyDialogueHasTextFromCirceAndAButton() {
    for (Kind kind : Kind.values()) {
      BlackjackDialogue dialogue = BlackjackDialogue.of(kind);

      assertEquals(kind, dialogue.getKind());
      assertNotNull(dialogue.getText());
      assertFalse(dialogue.getText().isBlank());
      assertFalse(dialogue.getButtonText().isBlank());
    }
    assertEquals("Serci", BlackjackDialogue.SPEAKER);
  }

  @Test
  void continueAfterAWinRunsTheCutsceneAndLeavesTheMatchAlone() {
    Blackjack game = matchEndedWith(MatchResult.PLAYER_WON, 100);
    AtomicInteger cutsceneStarts = new AtomicInteger();

    BlackjackDialogue.of(Kind.MATCH_WON).pressButton(game, cutsceneStarts::incrementAndGet);

    assertEquals(1, cutsceneStarts.get());
    assertEquals(MatchResult.PLAYER_WON, game.getMatchResult());
  }

  @Test
  void continueAfterAWinWithNoCutsceneDoesNothing() {
    Blackjack game = matchEndedWith(MatchResult.PLAYER_WON, 100);

    BlackjackDialogue.of(Kind.MATCH_WON).pressButton(game, null);

    assertEquals(MatchResult.PLAYER_WON, game.getMatchResult());
  }

  @Test
  void retryRestartsTheMatchKeepingTheRemainingGold() {
    Blackjack game = matchEndedWith(MatchResult.DEALER_WON, 100);
    int goldLeft = game.getBalance();

    BlackjackDialogue.of(Kind.MATCH_LOST).pressButton(game, null);

    assertEquals(goldLeft, game.getBalance());
    assertEquals(0, game.getPlayerWins());
    assertEquals(0, game.getDealerWins());
    assertEquals(MatchResult.IN_PROGRESS, game.getMatchResult());
    assertTrue(game.getPlayerHand().isEmpty());
    assertNotNull(playRound(game));
  }

  @Test
  void acceptingTheGoldRestartsTheMatchWithTheRefill() {
    Blackjack game = gameThatRanOutOfGold();

    BlackjackDialogue.of(Kind.OUT_OF_GOLD).pressButton(game, null);

    assertEquals(Blackjack.REFILL_GOLD, game.getBalance());
    assertEquals(0, game.getPlayerWins());
    assertEquals(0, game.getDealerWins());
    assertEquals(MatchResult.IN_PROGRESS, game.getMatchResult());
    assertNotNull(playRound(game));
  }

  @Test
  void winCutsceneStartsCutsceneFiveThenGoesToLevelThree() {
    GdxGame gdxGame = mock(GdxGame.class);
    CutsceneLoader loader = mock(CutsceneLoader.class);
    CutsceneLoader.Result result = mock(CutsceneLoader.Result.class);
    CutsceneLoader.LoadedCutscene cutscene = mock(CutsceneLoader.LoadedCutscene.class);
    when(loader.load("cutscene5")).thenReturn(result);
    when(result.isSuccess()).thenReturn(true);
    when(result.getCutscene()).thenReturn(cutscene);

    BlackjackDialogue.startWinCutscene(gdxGame, loader);

    verify(gdxGame).startCutscene(cutscene, GdxGame.ScreenType.LEVEL_3_GAME);
    verify(gdxGame, never()).transitionTo(GdxGame.ScreenType.LEVEL_3_GAME);
  }

  @Test
  void winCutsceneFallsBackToLevelThreeWhenItCannotBeLoaded() {
    GdxGame gdxGame = mock(GdxGame.class);
    CutsceneLoader loader = mock(CutsceneLoader.class);
    CutsceneLoader.Result result = mock(CutsceneLoader.Result.class);
    when(loader.load("cutscene5")).thenReturn(result);
    when(result.isSuccess()).thenReturn(false);
    when(result.getError()).thenReturn("missing");

    BlackjackDialogue.startWinCutscene(gdxGame, loader);

    verify(gdxGame).transitionTo(GdxGame.ScreenType.LEVEL_3_GAME);
  }

  private static RoundOutcome playRound(Blackjack game) {
    game.startNewRound();
    if (!game.isRoundOver()) {
      game.stand();
    }
    return game.getLastOutcome();
  }

  /** Plays seeded games until one ends in the given match result. */
  private static Blackjack matchEndedWith(MatchResult wanted, int startingGold) {
    for (int seed = 0; seed < 500; seed++) {
      Blackjack game = new Blackjack(startingGold, new Random(seed));
      int rounds = 0;
      while (!game.isMatchOver() && rounds++ < 100) {
        playRound(game);
      }
      if (game.getMatchResult() == wanted) {
        return game;
      }
    }
    throw new AssertionError("Could not find a match that ended in " + wanted);
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
}
