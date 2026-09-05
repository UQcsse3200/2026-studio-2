package com.csse3200.game.components.minigames.spinthewheel;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.BlurredBackdropDisplay;
import com.csse3200.game.ui.ScreenBlur;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows the spin the wheel minigame over the game, pausing it and blurring the frame the wheel
 * opened on. Owned by the screen showing the game, not by an entity.
 */
public class SpinTheWheelOverlay {
  private static final Logger logger = LoggerFactory.getLogger(SpinTheWheelOverlay.class);

  private final List<WheelItem> items;
  private boolean openRequested = false;
  private Entity overlay;

  /**
   * @param items the items to show on the wheel
   */
  public SpinTheWheelOverlay(List<WheelItem> items) {
    this.items = items;
  }

  /** Asks for the wheel to open. It appears at the end of the current frame. */
  public void request() {
    if (overlay != null || openRequested) {
      return;
    }
    logger.debug("Opening the wheel overlay");
    openRequested = true;
    ServiceLocator.getEntityService().setPaused(true);
  }

  /**
   * Opens the wheel if it has been asked for. Must be called at the end of the screen's render,
   * once the game has been drawn, so that the backdrop captures it.
   */
  public void afterRender() {
    if (!openRequested) {
      return;
    }
    openRequested = false;
    open();
  }

  private void open() {
    BlurredBackdropDisplay backdrop = new BlurredBackdropDisplay(ScreenBlur.capture());
    SpinTheWheelDisplay display = new SpinTheWheelDisplay(items);

    overlay =
        new Entity()
            .addComponent(backdrop)
            .addComponent(display)
            .addComponent(new SpinTheWheelOverlayActions(this::close));
    ServiceLocator.getEntityService().register(overlay);

    // Explicit, because Entity.create() runs components in hash order, not the order added.
    backdrop.toFront();
    display.toFront();
  }

  private void close() {
    ServiceLocator.getEntityService().scheduleRemoval(overlay);
    overlay = null;
    ServiceLocator.getEntityService().setPaused(false);
  }
}
