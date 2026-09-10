package com.csse3200.game.components.mainmenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens to events relevant to the Main Menu Screen and does something when one of the
 * events is triggered.
 */
public class MainMenuActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuActions.class);
  private GdxGame game;

  public MainMenuActions(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("Play", this::onPlay);
    entity.getEvents().addListener("Continue", this::onContinue);
    entity.getEvents().addListener("Minigames", this::onMinigames);
    entity.getEvents().addListener("Sandbox", this::onSandbox);
    entity.getEvents().addListener("Settings", this::onSettings);
    entity.getEvents().addListener("Exit", this::onExit);
  }

  /** Swaps to the Tutorial Game screen. */
  private void onPlay() {
    logger.info("Play");
    game.setScreen(GdxGame.ScreenType.TUTORIAL_GAME);
  }

  /** Intended for loading a saved game state. Load functionality is not actually implemented. */
  private void onContinue() {
    logger.info("Continue");
  }

  /** Swaps to the Mini Games Screen */
  private void onMinigames() {
    logger.info("Minigames");
    game.setScreen(GdxGame.ScreenType.MINIGAME_SELECT);
  }

  /** Swaps to the Sandbox screen. */
  private void onSandbox() {
    logger.info("Sandbox");
    game.setScreen(GdxGame.ScreenType.SANDBOX);
  }

  /** Swaps to the Settings screen. */
  private void onSettings() {
    logger.info("Settings");
    game.setScreen(GdxGame.ScreenType.SETTINGS);
  }

  /** Exits the game. */
  private void onExit() {
    logger.info("Exit");
    game.exit();
  }
}
