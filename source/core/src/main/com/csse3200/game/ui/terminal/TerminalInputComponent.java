package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.InputProcessor;
import com.csse3200.game.input.InputComponent;

/**
 * What the keyboard and touch debug terminal handlers have in common: while the terminal is open,
 * typing goes into it and every other input is swallowed, so it does not also drive the game.
 * Subclasses decide how the terminal is opened and closed.
 */
public abstract class TerminalInputComponent extends InputComponent {
  protected Terminal terminal;

  protected TerminalInputComponent() {
    super(10);
  }

  protected TerminalInputComponent(Terminal terminal) {
    this();
    this.terminal = terminal;
  }

  @Override
  public void create() {
    super.create();
    terminal = entity.getComponent(Terminal.class);
  }

  /**
   * Handles input if the terminal is open. This is because keyDown events are triggered alongside
   * keyTyped events. If the user is typing in the terminal, the input shouldn't trigger any other
   * input handlers.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyDown(int)
   */
  @Override
  public boolean keyDown(int keycode) {
    return terminal.isOpen();
  }

  /**
   * Handles input if the terminal is open. If 'enter' is typed, the entered message will be
   * processed, otherwise the message will be updated with the new character.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyTyped(char)
   */
  @Override
  public boolean keyTyped(char character) {
    if (!terminal.isOpen()) {
      return false;
    }

    if (character == '\b') {
      terminal.handleBackspace();
      return true;
    } else if (character == '\r' || character == '\n') {
      if (terminal.processMessage()) {
        terminal.toggleIsOpen();
      }
      terminal.setEnteredMessage("");
      return true;
    } else if (Character.isLetterOrDigit(character) || character == ' ') {
      // append character to message
      terminal.appendToMessage(character);
      return true;
    }
    return false;
  }

  /**
   * Handles input if the terminal is open. This is because keyUp events are triggered alongside
   * keyTyped events. If the user is typing in the terminal, the input shouldn't trigger any other
   * input handlers.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyUp(int)
   */
  @Override
  public boolean keyUp(int keycode) {
    return terminal.isOpen();
  }

  /**
   * Swallows pointer input while the terminal is open so clicks and taps don't attack, shoot, or
   * drive other overlays while typing.
   */
  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return terminal.isOpen();
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return terminal.isOpen();
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return terminal.isOpen();
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return terminal.isOpen();
  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    return terminal.isOpen();
  }
}
