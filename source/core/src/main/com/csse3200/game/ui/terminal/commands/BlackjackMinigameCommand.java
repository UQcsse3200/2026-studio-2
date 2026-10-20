package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.GdxGame;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads the Blackjack minigame from the debug terminal. */
public class BlackjackMinigameCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(BlackjackMinigameCommand.class);

  private final GdxGame game;

  /**
   * Creates the command.
   *
   * @param game game used to switch to the Blackjack screen
   */
  public BlackjackMinigameCommand(GdxGame game) {
    this.game = game;
  }

  /**
   * Switches to the Blackjack screen.
   *
   * @param args must be exactly {@code ["load"]}
   * @return whether the arguments were valid and the screen was switched
   */
  @Override
  public boolean action(ArrayList<String> args) {
    if (args.size() != 1 || !args.getFirst().equals("load")) {
      logger.debug("Invalid arguments received for 'blackjackMinigame' command: {}", args);
      return false;
    }

    logger.info("Loading Blackjack minigame");
    game.setScreen(GdxGame.ScreenType.MINIGAME_BLACKJACK);
    return true;
  }
}
