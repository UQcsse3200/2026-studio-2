package com.csse3200.game.components.minigames.blackjack;

import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.BlurredBackdropDisplay;
import com.csse3200.game.ui.ScreenBlur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shows the Blackjack minigame over the game, pausing it while the overlay is open. */
public class BlackjackOverlay {
  private static final Logger logger = LoggerFactory.getLogger(BlackjackOverlay.class);

  private boolean openRequested = false;
  private Entity overlay;
  private final InventoryComponent inventory;

  public BlackjackOverlay() {
    this(null);
  }

  public BlackjackOverlay(Entity player) {
    inventory = player == null ? null : player.getComponent(InventoryComponent.class);
  }

  /** Asks for Blackjack to open at the end of the current frame. */
  public void request() {
    if (overlay != null || openRequested) {
      return;
    }
    logger.debug("Opening the wheel overlay");
    openRequested = true;
    ServiceLocator.getEntityService().setPaused(true);
  }

  /** Opens Blackjack after the game has rendered the frame used as its backdrop. */
  public void afterRender() {
    if (!openRequested) {
      return;
    }
    openRequested = false;
    open();
  }

  private void open() {
    BlurredBackdropDisplay backdrop = new BlurredBackdropDisplay(ScreenBlur.capture());
    BlackjackDisplay display = new BlackjackDisplay(new Blackjack(100), inventory);

    overlay =
        new Entity()
            .addComponent(backdrop)
            .addComponent(display)
            .addComponent(new BlackjackOverlayActions(this::close));
    ServiceLocator.getEntityService().register(overlay);

    backdrop.toFront();
    display.toFront();
  }

  private void close() {
    ServiceLocator.getEntityService().scheduleRemoval(overlay);
    overlay = null;
    ServiceLocator.getEntityService().setPaused(false);
  }
}
