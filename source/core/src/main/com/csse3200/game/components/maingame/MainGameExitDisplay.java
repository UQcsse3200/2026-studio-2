package com.csse3200.game.components.maingame;

import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.ExitButtonDisplay;

/** Displays a button to exit the Main Game screen to the Main Menu screen. */
public class MainGameExitDisplay extends ExitButtonDisplay {
  public MainGameExitDisplay() {
    super(220f, 80f, 90f, 20f);
  }

  @Override
  protected void onExitClicked() {
    ButtonSound.playClickThen(() -> entity.getEvents().trigger("exit"));
  }
}
