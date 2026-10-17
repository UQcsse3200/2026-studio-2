package com.csse3200.game.screens;

import com.csse3200.game.GdxGame;

/** The settings screen opened from the pause menu, which goes back to the game when exited. */
public class SettingsFromPauseScreen extends SettingsScreen {
  public SettingsFromPauseScreen(GdxGame game) {
    super(game, GdxGame.ScreenType.LEVEL_1_GAME);
  }
}
