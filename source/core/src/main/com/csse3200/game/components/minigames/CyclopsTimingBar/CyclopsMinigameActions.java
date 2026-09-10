package com.csse3200.game.components.minigames.CyclopsTimingBar;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameActions.class);
  private final GdxGame game;

  public CyclopsMinigameActions(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("exit", this::onExit);
  }

  /** Swaps back to the Main Menu screen. */
  private void onExit() {
    logger.info("Returning to minigame select menu");
    game.setScreen(GdxGame.ScreenType.MINIGAME_SELECT);
  }
}
