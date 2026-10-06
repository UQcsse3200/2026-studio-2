package com.csse3200.game.components.minigames;

import com.badlogic.gdx.Input;
import com.csse3200.game.input.InputComponent;

/** Blocks normal gameplay keyboard input while a minigame overlay is active. */
public class MinigameOverlayInputComponent extends InputComponent {
  private final Runnable onClose;
  private final boolean allowTyping;

  public MinigameOverlayInputComponent(Runnable onClose) {
    this(onClose, false);
  }

  public MinigameOverlayInputComponent(Runnable onClose, boolean allowTyping) {
    super(100);
    this.onClose = onClose;
    this.allowTyping = allowTyping;
  }

  @Override
  public boolean keyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE) {
      onClose.run();
    }

    // Always block gameplay key presses while the overlay is open.
    return true;
  }

  @Override
  public boolean keyUp(int keycode) {
    // Always block gameplay key releases while the overlay is open.
    return true;
  }

  @Override
  public boolean keyTyped(char character) {
    // Blackjack needs typed characters to reach its bet TextField.
    return !allowTyping;
  }
}
