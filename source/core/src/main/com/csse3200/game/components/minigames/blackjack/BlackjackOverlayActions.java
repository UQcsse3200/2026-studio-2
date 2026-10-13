package com.csse3200.game.components.minigames.blackjack;

import com.csse3200.game.components.Component;

/** Handles commands for the Blackjack overlay. */
public class BlackjackOverlayActions extends Component {
  private final Runnable onClose;

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
