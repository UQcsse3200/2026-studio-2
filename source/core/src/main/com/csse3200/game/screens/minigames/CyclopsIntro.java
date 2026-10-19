package com.csse3200.game.screens.minigames;

import com.csse3200.game.components.TextBoxComponent;

/**
 * Owns the cyclops intro text box and decides when the minigame should start. A null text box means
 * the intro was skipped, so the minigame starts straight away.
 */
final class CyclopsIntro {
  private final TextBoxComponent textBox;
  private boolean startPending = true;

  CyclopsIntro(TextBoxComponent textBox) {
    this.textBox = textBox;
  }

  boolean isShowing() {
    return textBox != null && !textBox.isDismissed();
  }

  void advance() {
    if (isShowing()) {
      textBox.advance();
    }
  }

  /** Returns true once, when the intro is done (or was skipped) and the minigame should start. */
  boolean takeStartDue() {
    if (startPending && (textBox == null || textBox.isDismissed())) {
      startPending = false;
      return true;
    }
    return false;
  }
}
