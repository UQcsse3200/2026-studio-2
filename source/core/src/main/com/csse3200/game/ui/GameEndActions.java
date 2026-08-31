package com.csse3200.game.ui;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Handles button events raised by the Game End display. */
public class GameEndActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(GameEndActions.class);
  private final GdxGame game;

  public GameEndActions(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("mainMenu", this::onMainMenu);
    entity.getEvents().addListener("restart", this::onRestart);
    entity.getEvents().addListener("exitDesktop", this::onExitDesktop);
  }

  /** Returns the player to the main menu. */
  private void onMainMenu() {
    logger.info("Returning to main menu from game end screen");
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }

  /** Hook for a future reset/restart system. */
  private void onRestart() {
    logger.info("Restart requested. No reset system is currently implemented.");
    // TODO: connect this to the eventual game-reset flow.
  }

  /** Exits the application. */
  private void onExitDesktop() {
    logger.info("Exiting game from game end screen");
    game.exit();
  }
}
