package com.csse3200.game.ui.terminal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.input.InputService;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class TouchTerminalInputComponentTest extends AbstractTerminalInputComponentTest {
  @Test
  void createdInputRegistersAndUsesItsEntityTerminal() {
    InputService inputs = mock(InputService.class);
    ServiceLocator.registerInputService(inputs);
    Terminal terminal = new Terminal();
    TouchTerminalInputComponent input = new TouchTerminalInputComponent();
    new Entity().addComponent(terminal).addComponent(input);
    input.create();
    verify(inputs).register(input);
    assertEquals(10, input.getPriority());
    assertFalse(input.keyTyped('a'));
    input.scrolled(0f, -1f);
    assertTrue(terminal.isOpen());
    input.scrolled(1f, 0f);
    assertTrue(terminal.isOpen());
    assertTrue(input.keyTyped('a'));
    assertEquals("a", terminal.getEnteredMessage());
    input.dispose();
    verify(inputs).unregister(input);
  }

  @Override
  TerminalInputComponent createInput(Terminal terminal) {
    return new TouchTerminalInputComponent(terminal);
  }

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
