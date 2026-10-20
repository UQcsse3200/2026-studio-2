package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.csse3200.game.entities.configs.Portrait;
import com.csse3200.game.entities.factories.TextBoxFactory;
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
  private final Runnable onFinished;
  private int step = 0;
  private boolean seenA = false;
  private boolean seenD = false;
  private boolean finished = false;
  private boolean disposed = false;

  public TutorialComponent(TextBoxComponent textBox) {
    this(textBox, () -> {});
  }

  public TutorialComponent(TextBoxComponent textBox, Runnable onFinished) {
    super(6);
    this.textBox = textBox;
    this.onFinished = onFinished == null ? () -> {} : onFinished;
  }

  /** Creates and starts the tutorial using the portrait textbox factory. */
  public static TutorialComponent start(Runnable onFinished) {
    TextBoxComponent textBox = new TextBoxFactory().createPortraitTextBox(Portrait.CALYPSO_HAPPY);
    if (textBox == null) {
      return null;
    }
    TutorialComponent tutorial = new TutorialComponent(textBox, onFinished);
    tutorial.create();
    return tutorial;
  }

  @Override
  public boolean keyDown(int keycode) {
    if (!finished && textBox != null && !textBox.isDismissed() && !Terminal.isAnyOpen()) {
      handleTutorialKey(keycode);
    }
    // Never consume the key, so the player still moves while following the tutorial.
    return false;
  }

  private void handleTutorialKey(int keycode) {
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
    onFinished.run();
    // Removing this input handler during keyDown can skip the player's SHIFT handler.
    Gdx.app.postRunnable(this::dispose);
  }

  @Override
  public void dispose() {
    if (disposed) {
      return;
    }
    disposed = true;
    if (textBox != null && !textBox.isDismissed()) {
      textBox.dismiss();
    }
    super.dispose();
  }
}
