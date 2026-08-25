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
    entity.getEvents().addListener("play", this::onPlay);
    entity.getEvents().addListener("continue", this::onLoad);
    entity.getEvents().addListener("minigames", this::onMinigames);
    entity.getEvents().addListener("settings", this::onSettings);
    entity.getEvents().addListener("exit", this::onExit);
  }

  /** Swaps to the Main Game screen. */
  private void onPlay() {
    logger.info("play");
    game.setScreen(GdxGame.ScreenType.MAIN_GAME);
  }

  /** Intended for loading a saved game state. Load functionality is not actually implemented. */
  private void onLoad() {
    logger.info("continue");
  }

  /** Swaps to the Mini Games Screen */
  private void onMinigames() {
    logger.info("minigames");
    game.setScreen(GdxGame.ScreenType.MINIGAME_SELECT);
  } // TODO: uncomment when PR #37 is merged

  /** Swaps to the Settings screen. */
  private void onSettings() {
    logger.info("settings");
    game.setScreen(GdxGame.ScreenType.SETTINGS);
  }

  /** Exits the game. */
  private void onExit() {
    logger.info("exit");
    game.exit();
  }
}
