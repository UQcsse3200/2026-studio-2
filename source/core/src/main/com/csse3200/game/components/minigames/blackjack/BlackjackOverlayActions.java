package com.csse3200.game.components.minigames.blackjack;

import com.csse3200.game.components.Component;

/** Handles commands for the Blackjack overlay. */
public class BlackjackOverlayActions extends Component {
  private final Runnable onClose;

  /**
   * Creates the overlay's actions.
   *
   * @param onClose run when the "back" event closes the overlay
   */
  public BlackjackOverlayActions(Runnable onClose) {
    this.onClose = onClose;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("back", this::close);
  }

  private void close() {
    onClose.run();
  }
}
