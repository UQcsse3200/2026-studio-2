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
    entity.getEvents().addListener("Sandbox", this::onSandbox);
    entity.getEvents().addListener("Settings", this::onSettings);
    entity.getEvents().addListener("Exit", this::onExit);
  }

  /**
   * Starts a brand-new game: creates a save in the first empty slot, then the intro cutscene and
   * level 1. When every slot is taken the save-select screen opens so one can be deleted.
   */
  private void onPlay() {
    logger.info("Play");
    game.startNewGame();
  }

  /** Opens the save-select screen, where an existing save can be loaded or deleted. */
  private void onContinue() {
    logger.info("Continue");
    game.setScreen(GdxGame.ScreenType.LOAD_SAVE);
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

  private void onExit() {
    logger.info("Exit");
    game.exit();
  }
}
