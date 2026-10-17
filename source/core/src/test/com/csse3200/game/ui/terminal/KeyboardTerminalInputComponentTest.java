package com.csse3200.game.ui.terminal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.npc.ShopNpcComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.components.player.PlayerInteractionComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class KeyboardTerminalInputComponentTest extends AbstractTerminalInputComponentTest {
  @Override
  TerminalInputComponent createInput(Terminal terminal) {
    return new KeyboardTerminalInputComponent(terminal);
  }

  @Test
  void shouldSwallowScrollingWhenTerminalOpen() {
    Terminal terminal = mock(Terminal.class);
    when(terminal.isOpen()).thenReturn(true);

    assertTrue(createInput(terminal).scrolled(0f, 1f));
  }

  @Test
  void shouldNotSwallowScrollingWhenTerminalClosed() {
    Terminal terminal = mock(Terminal.class);
    when(terminal.isOpen()).thenReturn(false);

    assertFalse(createInput(terminal).scrolled(0f, 1f));
  }

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

  /** Registers the services, and has a player beside a shop NPC open the shop. */
  private EntityService openShopWith(PlayerInteractionComponent interaction) {
    EntityService entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);
    ServiceLocator.registerPhysicsService(new PhysicsService());

    Entity player = new Entity().addComponent(new InventoryComponent(0)).addComponent(interaction);
    player.setPosition(0f, 0f);
    entityService.register(player);

    Entity shopNpc = new Entity().addComponent(new ShopNpcComponent());
    shopNpc.setPosition(0.5f, 0f);
    entityService.register(shopNpc);

    player.getEvents().trigger("interact");
    assertTrue(interaction.isShopOpen());
    return entityService;
  }

  @Test
  void shouldSwallowEscapeWithoutClosingShopWhenTerminalOpen() {
    PlayerInteractionComponent interaction = new PlayerInteractionComponent();
    openShopWith(interaction);

    Terminal terminal = new Terminal();
    terminal.setOpen();
    KeyboardTerminalInputComponent terminalInput = new KeyboardTerminalInputComponent(terminal);

    // Consumed, but the shop stays open: F1 is the only way out while the terminal is up.
    assertTrue(terminalInput.keyDown(Input.Keys.ESCAPE));
    assertTrue(interaction.isShopOpen());
  }

  @Test
  void shouldReleaseHeldPlayerInputWhenTerminalOpened() {
    ServiceLocator.registerInputService(new InputService());
    EntityService entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);

    Entity player = new Entity().addComponent(new KeyboardPlayerInputComponent());
    entityService.register(player);
    AtomicBoolean released = new AtomicBoolean(false);
    player.getEvents().addListener("releaseHeldGameplayInput", () -> released.set(true));

    Terminal terminal = new Terminal();
    KeyboardTerminalInputComponent terminalInput = new KeyboardTerminalInputComponent(terminal);

    terminalInput.keyDown(Input.Keys.F1);
    assertTrue(terminal.isOpen());
    assertTrue(released.get());

    released.set(false);
    terminalInput.keyDown(Input.Keys.F1);
    assertFalse(terminal.isOpen());
    assertFalse(released.get());
  }

  @Test
  void escapeShouldCloseShopInsteadOfTogglingPause() {
    PlayerInteractionComponent interaction = new PlayerInteractionComponent();
    EntityService entityService = openShopWith(interaction);
    assertFalse(entityService.getPaused());

    Terminal terminal = new Terminal();
    KeyboardTerminalInputComponent terminalInput = new KeyboardTerminalInputComponent(terminal);
    new Entity().addComponent(terminalInput);

    assertTrue(terminalInput.keyDown(Input.Keys.ESCAPE));
    assertFalse(interaction.isShopOpen());
    assertFalse(entityService.getPaused());
  }
}
