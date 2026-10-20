package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.GdxGame;
import com.csse3200.game.cutscene.CutsceneLoader;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads the Blackjack minigame from the debug terminal, after the cutscene that leads into it. */
public class BlackjackMinigameCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(BlackjackMinigameCommand.class);

  /** Cutscene played before the Blackjack match. */
  static final String INTRO_CUTSCENE = "cutscene4";

  private final GdxGame game;
  private final CutsceneLoader loader;

  /**
   * Creates the command.
   *
   * @param game game used to play the cutscene and switch to the Blackjack screen
   * @param loader loads the cutscene played before Blackjack
   */
  public BlackjackMinigameCommand(GdxGame game, CutsceneLoader loader) {
    this.game = game;
    this.loader = loader;
  }

  /**
   * Creates the command.
   *
   * @param game game used to play the cutscene and switch to the Blackjack screen
   */
  public BlackjackMinigameCommand(GdxGame game) {
    this(game, new CutsceneLoader());
  }

  /**
   * Plays the cutscene that leads into Blackjack, then switches to the Blackjack screen. If the
   * cutscene cannot be loaded, the Blackjack screen is shown straight away.
   *
   * @param args must be exactly {@code ["load"]}
   * @return whether the arguments were valid and the minigame was started
   */
  @Override
  public boolean action(ArrayList<String> args) {
    if (args.size() != 1 || !args.getFirst().equals("load")) {
      logger.debug("Invalid arguments received for 'blackjackMinigame' command: {}", args);
      return false;
    }

    logger.info("Loading Blackjack minigame");
    CutsceneLoader.Result result = loader.load(INTRO_CUTSCENE);
    if (result.isSuccess()) {
      game.startCutscene(result.getCutscene(), GdxGame.ScreenType.MINIGAME_BLACKJACK);
    } else {
      logger.warn("Cutscene {} unavailable: {}", INTRO_CUTSCENE, result.getError());
      game.setScreen(GdxGame.ScreenType.MINIGAME_BLACKJACK);
    }
    return true;
  }
}
