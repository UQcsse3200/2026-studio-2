package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.PlayerInteractionComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;

/**
 * Input handler for the debug terminal for keyboard and touch (mouse) input. This input handler
 * only uses keyboard input.
 *
 * <p>The debug terminal can be opened and closed by pressing 'F1' and a message can be entered via
 * the keyboard.
 */
public class KeyboardTerminalInputComponent extends TerminalInputComponent {
  private static final int TOGGLE_OPEN_KEY = Input.Keys.F1;

  public KeyboardTerminalInputComponent() {
    super();
  }

  public KeyboardTerminalInputComponent(Terminal terminal) {
    super(terminal);
  }

  /**
   * If the toggle key is pressed, the terminal will open / close. Otherwise, handles input if the
   * terminal is open. This is because keyDown events are triggered alongside keyTyped events. If
   * the user is typing in the terminal, the input shouldn't trigger any other input handlers. While
   * the terminal is open every key is swallowed, including ESC — F1 is the only way out.
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
    if (terminal.isOpen()) {
      return true;
    }
    // Shop close stays here because this handler runs before player input, which swallows keys
    // while the shop is open. Pause itself is owned by the pause overlay on main.
    return keycode == Input.Keys.ESCAPE && closeOpenShop();
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
}
