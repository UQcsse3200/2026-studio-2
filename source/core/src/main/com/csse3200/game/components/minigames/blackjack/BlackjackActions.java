package com.csse3200.game.components.minigames.blackjack;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles actions for the Blackjack screen.
 */
public class BlackjackActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(BlackjackActions.class);
  private final GdxGame game;

  public BlackjackActions(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("back", this::onBack);
  }

  private void onBack() {
    logger.info("Returning to minigame select");
    game.setScreen(GdxGame.ScreenType.MINIGAME_SELECT);
  }
}
