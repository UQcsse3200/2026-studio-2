package com.csse3200.game.components;

import com.badlogic.gdx.Input.Keys;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.ui.terminal.Terminal;

/**
 * Observes raw key presses to advance a tutorial textbox through a fixed sequence.
 *
 * <p>Steps: press A and D for movement, then SPACE to jump, then SHIFT to dash/sprint. Movement is
 * never blocked, this component always returns false so the player input handler still receives
 * every key. Priority exceeds the player handler so keyDown is seen before the player consumes it.
 */
public class TutorialComponent extends InputComponent {
  private final TextBoxComponent textBox;
  private int step = 0;
  private boolean seenA = false;
  private boolean seenD = false;
  private boolean finished = false;

  public TutorialComponent(TextBoxComponent textBox) {
    super(6);
    this.textBox = textBox;
  }

  @Override
  public boolean keyDown(int keycode) {
    if (finished || textBox == null || textBox.isDismissed() || Terminal.isAnyOpen()) {
      return false;
    }

    switch (step) {
      case 0:
        if (keycode == Keys.A) {
          seenA = true;
        } else if (keycode == Keys.D) {
          seenD = true;
        }
        if (seenA && seenD) {
          advanceStep();
        }
        break;
      case 1:
        if (keycode == Keys.SPACE) {
          advanceStep();
        }
        break;
      case 2:
        if (keycode == Keys.SHIFT_LEFT || keycode == Keys.SHIFT_RIGHT) {
          finishTutorial();
        }
        break;
      default:
        break;
    }
    return false;
  }

  private void advanceStep() {
    textBox.revealCurrentPage();
    textBox.advance();
    step++;
  }

  private void finishTutorial() {
    textBox.revealCurrentPage();
    textBox.advance();
    textBox.dismiss();
    finished = true;
    dispose();
  }

  @Override
  public void dispose() {
    if (textBox != null && !textBox.isDismissed()) {
      textBox.dismiss();
    }
    super.dispose();
  }
}
