package com.csse3200.game.components.minigames.spinthewheel;

import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to events relevant to the wheel when it is shown as an overlay over the game. Closes the
 * overlay rather than changing screen, which {@link SpinTheWheelActions} does for the standalone
 * minigame screen.
 */
public class SpinTheWheelOverlayActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(SpinTheWheelOverlayActions.class);
  private final Runnable onClose;

  /**
   * @param onClose what to run when the player closes the wheel
   */
  public SpinTheWheelOverlayActions(Runnable onClose) {
    this.onClose = onClose;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("back", this::onBack);
  }

  private void onBack() {
    logger.info("Closing the wheel overlay");
    onClose.run();
  }
}