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
    entity.getEvents().addListener("Settings", this::onSettings);
    entity.getEvents().addListener("Exit", this::onExit);
  }

  /** Fades into the new narrative intro level. */
  private void onPlay() {
    logger.info("Play");
    game.transitionTo(GdxGame.ScreenType.INTRO_TUTORIAL);
  }

  private void onContinue() {
    logger.info("Continue");
  }

  private void onMinigames() {
    logger.info("Minigames");
    game.setScreen(GdxGame.ScreenType.MINIGAME_SELECT);
  }

  private void onSettings() {
    logger.info("Settings");
    game.setScreen(GdxGame.ScreenType.SETTINGS);
  }

  private void onExit() {
    logger.info("Exit");
    game.exit();
  }
}
