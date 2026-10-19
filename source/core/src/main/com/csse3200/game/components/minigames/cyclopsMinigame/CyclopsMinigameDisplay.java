package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.csse3200.game.ui.TopRightButtonDisplay;

/** Displays a button to leave the cyclops minigame. */
public class CyclopsMinigameDisplay extends TopRightButtonDisplay {
  public CyclopsMinigameDisplay() {
    super(
        160f, 56f, 10f, 10f, "images/Buttons/exit_up_btn.png", "images/Buttons/exit_down_btn.png");
  }

  @Override
  protected void onButtonClicked() {
    entity.getEvents().trigger("exit");
  }
}
