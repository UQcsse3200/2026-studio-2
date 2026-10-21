package com.csse3200.game.components.minigames.blackjack;

import com.csse3200.game.GdxGame;
import com.csse3200.game.cutscene.CutsceneLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The dialogue Circe speaks when a Blackjack match ends, and what its button does.
 *
 * <p>The three dialogues are picked from the finished match: the player won, the player lost but
 * still has gold, or the player ran out of gold. The display shows the text and button label, and
 * calls {@link #pressButton} when the button is clicked.
 */
public final class BlackjackDialogue {
  private static final Logger logger = LoggerFactory.getLogger(BlackjackDialogue.class);

  /** Who speaks the dialogue. */
  public static final String SPEAKER = "Cerci";

  /** Cutscene played after the player wins the match. */
  static final String WIN_CUTSCENE = "cutscene5";

  /** Screen the game goes to once the win cutscene ends. */
  static final GdxGame.ScreenType AFTER_WIN_SCREEN = GdxGame.ScreenType.LEVEL_3_GAME;

  /** Gold Circe gives the player as a gift when the match starts. */
  public static final int GIFT_GOLD = 5;

  /** What Circe can be saying: a welcome gift at the start, or how a finished match ended. */
  public enum Kind {
    GIFT,
    MATCH_WON,
    MATCH_LOST,
    OUT_OF_GOLD
  }

  private final Kind kind;
  private final String text;
  private final String buttonText;

  private BlackjackDialogue(Kind kind, String text, String buttonText) {
    this.kind = kind;
    this.text = text;
    this.buttonText = buttonText;
  }

  /**
   * Returns the dialogue for a finished match, or null while the match is still going. Running out
   * of gold takes priority over losing on rounds, because the player cannot retry without gold.
   *
   * @param blackjack the game whose match to check
   * @return the dialogue to show, or null if the match is not over
   */
  public static BlackjackDialogue forMatch(Blackjack blackjack) {
    Blackjack.MatchResult result = blackjack.getMatchResult();
    if (result == Blackjack.MatchResult.IN_PROGRESS) {
      return null;
    }
    if (result == Blackjack.MatchResult.PLAYER_WON) {
      return of(Kind.MATCH_WON);
    }
    return of(blackjack.getBalance() < Blackjack.BET ? Kind.OUT_OF_GOLD : Kind.MATCH_LOST);
  }

  /**
   * Returns the dialogue for a kind of match ending.
   *
   * @param kind how the match ended
   * @return the dialogue text and button for that ending
   */
  public static BlackjackDialogue of(Kind kind) {
    return switch (kind) {
      case GIFT ->
          new BlackjackDialogue(
              kind,
              "Welcome, Odysseus. Every guest of mine plays with a full purse. Take these "
                  + GIFT_GOLD
                  + " coins as a gift, and let us see how clever you are.",
              "Accept " + GIFT_GOLD + " gold");
      case MATCH_WON ->
          new BlackjackDialogue(
              kind,
              "Impressive, Odysseus. Your wit is as sharp as your blade. You have earned my"
                  + " respect. Now, let me help your men and reveal what lies ahead.",
              "Continue");
      case MATCH_LOST ->
          new BlackjackDialogue(
              kind,
              "Even the cleverest hero must taste defeat. Come, Odysseus. Gather your courage and"
                  + " challenge me once more.",
              "Retry");
      case OUT_OF_GOLD ->
          new BlackjackDialogue(
              kind,
              "Ah, Odysseus, you've lost all your gold! But fear not. Take these ten coins as a"
                  + " gift from me. Come, let us play again!",
              "Accept " + Blackjack.REFILL_GOLD + " gold");
    };
  }

  public Kind getKind() {
    return kind;
  }

  /** Returns what Circe says. */
  public String getText() {
    return text;
  }

  /** Returns the label of the button under the dialogue. */
  public String getButtonText() {
    return buttonText;
  }

  /**
   * Does what the button says. A win runs the cutscene, a loss restarts the match with the gold the
   * player has left, and running out of gold restarts it with the refill.
   *
   * <p>The caller should copy {@link Blackjack#getBalance()} back to the player's inventory
   * afterwards, since the refill changes the balance.
   *
   * @param blackjack the game to restart
   * @param startWinCutscene run when the player continues after a win, may be null
   */
  public void pressButton(Blackjack blackjack, Runnable startWinCutscene) {
    switch (kind) {
      case GIFT -> blackjack.setBalance(blackjack.getBalance() + GIFT_GOLD);
      case MATCH_WON -> runIfSet(startWinCutscene);
      case MATCH_LOST -> blackjack.restartMatchKeepingGold();
      case OUT_OF_GOLD -> blackjack.resetMatch();
    }
  }

  private static void runIfSet(Runnable action) {
    if (action != null) {
      action.run();
    }
  }

  /**
   * Plays the cutscene that follows a Blackjack win, then continues to the next level. If the
   * cutscene cannot be loaded the game goes straight to the next level.
   *
   * @param game the game to change screens on
   */
  public static void startWinCutscene(GdxGame game) {
    startWinCutscene(game, new CutsceneLoader());
  }

  static void startWinCutscene(GdxGame game, CutsceneLoader loader) {
    CutsceneLoader.Result result = loader.load(WIN_CUTSCENE);
    if (!result.isSuccess()) {
      logger.warn("Win cutscene unavailable: {}", result.getError());
      game.transitionTo(AFTER_WIN_SCREEN);
      return;
    }
    game.startCutscene(result.getCutscene(), AFTER_WIN_SCREEN);
  }
}
