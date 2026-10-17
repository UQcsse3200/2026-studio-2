package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.csse3200.game.components.ExitButtonDisplay;

/** Displays a button to leave the cyclops minigame. */
public class CyclopsMinigameDisplay extends ExitButtonDisplay {
  public CyclopsMinigameDisplay() {
    super(160f, 56f, 10f, 10f);
  }

  @Override
  protected void onExitClicked() {
    entity.getEvents().trigger("exit");
  }
}
