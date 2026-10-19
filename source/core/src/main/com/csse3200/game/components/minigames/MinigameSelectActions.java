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
   * Launches the chosen minigame. Each minigame adds its own screen and a case here as it lands.
   *
   * @param minigame the minigame the player selected
   */
  private void onSelectMinigame(MinigameType minigame) {
    GdxGame.ScreenType screen =
        switch (minigame) {
          case CYCLOPS_TIMING -> GdxGame.ScreenType.CYCLOPS_MINIGAME;
          case SPIN_THE_WHEEL -> GdxGame.ScreenType.MINIGAME_SPIN_THE_WHEEL;
          case BLACKJACK -> GdxGame.ScreenType.MINIGAME_BLACKJACK;
        };
    game.setScreen(screen);
    logger.info("Selected minigame {} loading...", minigame);
  }

  /** Swaps back to the Main Menu screen. */
  private void onBack() {
    logger.info("Returning to main menu");
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }
}
