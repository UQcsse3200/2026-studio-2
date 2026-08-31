package com.csse3200.game.components.minigames;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.screens.minigames.MinigameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens to events relevant to the Minigame Select Screen and does something when one
 * of the events is triggered.
 */
public class MinigameSelectActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(MinigameSelectActions.class);
  private final GdxGame game;

  public MinigameSelectActions(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("selectMinigame", this::onSelectMinigame);
    entity.getEvents().addListener("back", this::onBack);
  }

  /**
   * Launches the chosen minigame. No minigames are implemented yet; each one will add its own
   * screen and a case here as it lands.
   *
   * @param minigame the minigame the player selected
   */
  private void onSelectMinigame(MinigameType minigame) {
    switch (minigame) {
      case CYCLOPS_TIMING:
        game.setScreen(GdxGame.ScreenType.CYCLOPS_MINIGAME);
        logger.info("Selected minigame {} loading...", minigame);
        return;
      default:
        logger.info("Selected minigame {} is not implemented", minigame);
    }
    logger.info("Selected minigame {}, not yet implemented", minigame);
  }

  /** Swaps back to the Main Menu screen. */
  private void onBack() {
    logger.info("Returning to main menu");
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }
}
