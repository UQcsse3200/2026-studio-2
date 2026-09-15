package com.csse3200.game.components.maingame;

import com.csse3200.game.components.Component;

public class PauseMenuOverlayActions extends Component {
  private final Runnable onClose;

  /**
   * @param onClose what to run when the player closes the pause meny
   */
  public PauseMenuOverlayActions(Runnable onClose) {
    this.onClose = onClose;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("togglePause", this::onBack);
  }

  private void onBack() {
    onClose.run();
  }
}
