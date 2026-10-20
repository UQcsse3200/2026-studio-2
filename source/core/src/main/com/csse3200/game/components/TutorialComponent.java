package com.csse3200.game.components;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.entities.configs.TextConfig;
import com.csse3200.game.files.FileLoader;
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

  public TutorialComponent(TextBoxComponent textBox) {
    this(textBox, () -> {});
  }

  public TutorialComponent(TextBoxComponent textBox, Runnable onFinished) {
    super(6);
    this.textBox = textBox;
    this.onFinished = onFinished == null ? () -> {} : onFinished;
  }

  /** Loads and starts the configured tutorial without going through the terminal command. */
  public static TutorialComponent start(String configPath, Runnable onFinished) {
    TextConfig config = FileLoader.readClass(TextConfig.class, configPath);
    if (config == null) {
      return null;
    }
    TextBoxComponent textBox =
        new TextBoxComponent(
            config.xPos,
            config.yPos,
            config.getTextColour(),
            config.getBackgroundColour(),
            config.getBorderColour(),
            config.charsPerSecond,
            config.maxWidth,
            config.padding,
            config.borderThickness,
            config.fontPath,
            config.getTextAlignment(),
            config.pages,
            config.portraitPaths);
    textBox.setExternallyControlled(true);
    textBox.setPosition(config.xPos, config.yPos, Align.bottomLeft);
    textBox.create();

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
