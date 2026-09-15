package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.PlayerInteractionComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Input handler for the debug terminal for keyboard and touch (mouse) input. This input handler
 * only uses keyboard input.
 *
 * <p>The debug terminal can be opened and closed by pressing 'F1' and a message can be entered via
 * the keyboard.
 */
public class KeyboardTerminalInputComponent extends InputComponent {
  private static final int TOGGLE_OPEN_KEY = Input.Keys.F1;
  private Terminal terminal;

  public KeyboardTerminalInputComponent() {
    super(10);
  }

  public KeyboardTerminalInputComponent(Terminal terminal) {
    this();
    this.terminal = terminal;
  }

  @Override
  public void create() {
    super.create();
    terminal = entity.getComponent(Terminal.class);
  }

  /**
   * If the toggle key is pressed, the terminal will open / close.
   *
   * <p>Otherwise, handles input if the terminal is open. This is because keyDown events are
   * triggered alongside keyTyped events. If the user is typing in the terminal, the input shouldn't
   * trigger any other input handlers.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyDown(int)
   */
  @Override
  public boolean keyDown(int keycode) {
    // handle open and close terminal
    if (keycode == TOGGLE_OPEN_KEY) {
      terminal.toggleIsOpen();
      return true;
    }
    if (keycode == Input.Keys.ESCAPE) {
      if (closeOpenShop()) {
        return true;
      }

      EntityService entityService = ServiceLocator.getEntityService();
      if (entityService == null) {
        return terminal.isOpen();
      }

      entityService.togglePaused();

      if (entityService.getPaused()) {
        entity.getEvents().trigger("showPauseMenu");
      } else {
        entity.getEvents().trigger("hidePauseMenu");
      }
    }

    return terminal.isOpen();
  }

  /**
   * Closes the shop instead of toggling the pause menu, so gameplay stays paused until the shop
   * itself unpauses on close.
   *
   * @return true if an open shop was closed
   */
  private boolean closeOpenShop() {
    EntityService entityService = ServiceLocator.getEntityService();
    if (entityService == null) {
      return false;
    }

    Array<Entity> entities = entityService.getEntities();
    for (int i = 0; i < entities.size; i++) {
      Entity shopEntity = entities.get(i);
      PlayerInteractionComponent interaction =
          shopEntity.getComponent(PlayerInteractionComponent.class);
      if (interaction != null && interaction.isShopOpen()) {
        shopEntity.getEvents().trigger("closeShop");
        return true;
      }
    }
    return false;
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
}
