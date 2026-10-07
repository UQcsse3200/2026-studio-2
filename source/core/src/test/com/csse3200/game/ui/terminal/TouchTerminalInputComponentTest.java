package com.csse3200.game.ui.terminal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TouchTerminalInputComponentTest {
  @Test
  void shouldSetTerminalOpenClose() {
    Terminal terminal = spy(Terminal.class);
    TouchTerminalInputComponent terminalInput = new TouchTerminalInputComponent(terminal);

    terminal.setClosed();

    terminalInput.scrolled(0, -1);
    assertTrue(terminal.isOpen());

    terminalInput.scrolled(0, 1);
    assertFalse(terminal.isOpen());

    verify(terminal).setOpen();
    verify(terminal, times(2)).setClosed();
  }

  @Test
  void shouldUpdateMessageOnKeyTyped() {
    Terminal terminal = mock(Terminal.class);
    when(terminal.isOpen()).thenReturn(true);
    KeyboardTerminalInputComponent terminalInput = new KeyboardTerminalInputComponent(terminal);

    terminalInput.keyTyped('a');
    terminalInput.keyTyped('b');
    verify(terminal).appendToMessage('a');
    verify(terminal).appendToMessage('b');

    terminalInput.keyTyped('\b');
    verify(terminal).handleBackspace();

    terminalInput.keyTyped('\n');
    verify(terminal).processMessage();
  }

  @Test
  void shouldHandleMessageWhenTerminalOpen() {
    Terminal terminal = mock(Terminal.class);
    KeyboardTerminalInputComponent terminalInput = new KeyboardTerminalInputComponent(terminal);

    when(terminal.isOpen()).thenReturn(true);
    assertTrue(terminalInput.keyDown('a'));
    assertTrue(terminalInput.keyUp('a'));

    when(terminal.isOpen()).thenReturn(false);
    assertFalse(terminalInput.keyDown('a'));
    assertFalse(terminalInput.keyUp('a'));
  }

  @Test
  void shouldSwallowPointerInputWhenTerminalOpen() {
    Terminal terminal = mock(Terminal.class);
    when(terminal.isOpen()).thenReturn(true);
    TouchTerminalInputComponent terminalInput = new TouchTerminalInputComponent(terminal);

    assertTrue(terminalInput.touchDown(100, 100, 0, Input.Buttons.LEFT));
    assertTrue(terminalInput.touchUp(100, 100, 0, Input.Buttons.LEFT));
    assertTrue(terminalInput.touchDragged(100, 100, 0));
    assertTrue(terminalInput.mouseMoved(100, 100));
  }

  @Test
  void shouldNotSwallowPointerInputWhenTerminalClosed() {
    Terminal terminal = mock(Terminal.class);
    when(terminal.isOpen()).thenReturn(false);
    TouchTerminalInputComponent terminalInput = new TouchTerminalInputComponent(terminal);

    assertFalse(terminalInput.touchDown(100, 100, 0, Input.Buttons.LEFT));
    assertFalse(terminalInput.touchUp(100, 100, 0, Input.Buttons.LEFT));
    assertFalse(terminalInput.touchDragged(100, 100, 0));
    assertFalse(terminalInput.mouseMoved(100, 100));
  }

  @Test
  void shouldReleaseHeldPlayerInputWhenTerminalScrolledOpen() {
    ServiceLocator.registerInputService(new InputService());
    EntityService entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);

    Entity player = new Entity().addComponent(new KeyboardPlayerInputComponent());
    entityService.register(player);
    AtomicBoolean released = new AtomicBoolean(false);
    player.getEvents().addListener("releaseHeldGameplayInput", () -> released.set(true));

    Terminal terminal = new Terminal();
    TouchTerminalInputComponent terminalInput = new TouchTerminalInputComponent(terminal);

    terminalInput.scrolled(0, -1);
    assertTrue(terminal.isOpen());
    assertTrue(released.get());
  }
}
