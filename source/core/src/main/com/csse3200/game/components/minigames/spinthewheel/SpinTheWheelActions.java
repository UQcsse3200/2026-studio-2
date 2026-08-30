package com.csse3200.game.components.minigames.spinthewheel;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens to events relevant to the Spin the Wheel screen and does something when one of
 * the events is triggered.
 */
public class SpinTheWheelActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(SpinTheWheelActions.class);
  private final GdxGame game;

  public SpinTheWheelActions(GdxGame game) {
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
