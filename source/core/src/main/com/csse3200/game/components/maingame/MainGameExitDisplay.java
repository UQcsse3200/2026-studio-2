package com.csse3200.game.components.maingame;

import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.ui.TopRightButtonDisplay;

/** Displays a button to exit the Main Game screen to the Main Menu screen. */
public class MainGameExitDisplay extends TopRightButtonDisplay {
  public MainGameExitDisplay() {
    super(
        220f, 80f, 90f, 20f, "images/Buttons/exit_up_btn.png", "images/Buttons/exit_down_btn.png");
  }

  @Override
  protected void onButtonClicked() {
    ButtonSound.playClickThen(() -> entity.getEvents().trigger("exit"));
  }
}
