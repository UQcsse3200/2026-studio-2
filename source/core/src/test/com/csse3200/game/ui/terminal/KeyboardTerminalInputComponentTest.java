package com.csse3200.game.ui.terminal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.npc.ShopNpcComponent;
import com.csse3200.game.components.player.PlayerInteractionComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class KeyboardTerminalInputComponentTest {
  @Test
  void shouldToggleTerminalOpenClose() {
    Terminal terminal = spy(Terminal.class);
    KeyboardTerminalInputComponent terminalInput = new KeyboardTerminalInputComponent(terminal);

    terminal.setClosed();

    terminalInput.keyDown(Input.Keys.F1);
    assertTrue(terminal.isOpen());

    terminalInput.keyDown(Input.Keys.F1);
    assertFalse(terminal.isOpen());

    verify(terminal, times(2)).toggleIsOpen();
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
  void escapeShouldCloseShopInsteadOfTogglingPause() {
    EntityService entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);
    ServiceLocator.registerPhysicsService(new PhysicsService());

    PlayerInteractionComponent interaction = new PlayerInteractionComponent();
    Entity player = new Entity().addComponent(new InventoryComponent(0)).addComponent(interaction);
    player.setPosition(0f, 0f);
    entityService.register(player);

    Entity shopNpc = new Entity().addComponent(new ShopNpcComponent());
    shopNpc.setPosition(0.5f, 0f);
    entityService.register(shopNpc);

    player.getEvents().trigger("interact");
    assertTrue(interaction.isShopOpen());
    assertFalse(entityService.getPaused());

    Terminal terminal = new Terminal();
    KeyboardTerminalInputComponent terminalInput = new KeyboardTerminalInputComponent(terminal);
    new Entity().addComponent(terminalInput);

    assertTrue(terminalInput.keyDown(Input.Keys.ESCAPE));
    assertFalse(interaction.isShopOpen());
    assertFalse(entityService.getPaused());
  }
}
