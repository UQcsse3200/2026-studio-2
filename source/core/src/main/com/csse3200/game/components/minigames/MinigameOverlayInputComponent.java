package com.csse3200.game.components.minigames;

import com.badlogic.gdx.Input;
import com.csse3200.game.input.InputComponent;

/** Blocks normal gameplay keyboard input while a minigame overlay is active. */
public class MinigameOverlayInputComponent extends InputComponent {
  private final Runnable onClose;

  public MinigameOverlayInputComponent(Runnable onClose) {
    super(100);
    this.onClose = onClose;
  }

  @Override
  public boolean keyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE) {
      onClose.run();
    }
    return true;
  }

  @Override
  public boolean keyUp(int keycode) {
    return true;
  }

  @Override
  public boolean keyTyped(char character) {
    return true;
  }
}
