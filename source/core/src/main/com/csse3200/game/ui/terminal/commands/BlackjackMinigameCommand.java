package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.GdxGame;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads the Blackjack minigame from the debug terminal. */
public class BlackjackMinigameCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(BlackjackMinigameCommand.class);

  private final GdxGame game;
  private final GdxGame.ScreenType returnScreen;

  public BlackjackMinigameCommand(GdxGame game) {
    this(game, GdxGame.ScreenType.MINIGAME_SELECT);
  }

  public BlackjackMinigameCommand(GdxGame game, GdxGame.ScreenType returnScreen) {
    this.game = game;
    this.returnScreen = returnScreen;
  }

  @Override
  public boolean action(ArrayList<String> args) {
    if (args.size() != 1 || !args.getFirst().equals("load")) {
      logger.debug("Invalid arguments received for 'blackjackMinigame' command: {}", args);
      return false;
    }

    logger.info("Loading Blackjack minigame");
    game.setScreen(GdxGame.ScreenType.MINIGAME_BLACKJACK, returnScreen);
    return true;
  }
}
