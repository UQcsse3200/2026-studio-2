package com.csse3200.game.components.maingame;

import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.BlurredBackdropDisplay;
import com.csse3200.game.ui.ScreenBlur;

/**
 * Shows the spin the wheel minigame over the game, pausing it and blurring the frame the wheel
 * opened on. Owned by the screen showing the game, not by an entity.
 */
public class PauseMenuOverlay {
  private boolean open = false;
  private boolean openRequested = false;
  private Entity overlay;
  private GdxGame game;
  private GameArea area;

  public PauseMenuOverlay(GdxGame game, GameArea area) {
    this.game = game;
    this.area = area;
  }

  /** Asks for the wheel to open. It appears at the end of the current frame. */
  public void request() {
    if (openRequested) {
      return;
    }
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
    if (open) {
      close();
    } else {
      open();
    }
  }

  private void open() {
    System.out.println("opening");
    open = true;
    BlurredBackdropDisplay backdrop = new BlurredBackdropDisplay(ScreenBlur.capture());
    PauseMenuDisplay display = new PauseMenuDisplay(game, area);

    overlay =
        new Entity()
            .addComponent(backdrop)
            .addComponent(display)
            .addComponent(new PauseMenuOverlayActions(this::close));
    ServiceLocator.getEntityService().register(overlay);

    // Explicit, because Entity.create() runs components in hash order, not the order added.
    backdrop.toFront();
    display.toFront();
  }

  private void close() {
    System.out.println("closing");

    open = false;
    ServiceLocator.getEntityService().scheduleRemoval(overlay);
    overlay = null;
    ServiceLocator.getEntityService().setPaused(false);
  }
}
