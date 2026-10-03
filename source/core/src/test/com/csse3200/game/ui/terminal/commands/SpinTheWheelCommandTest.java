package com.csse3200.game.ui.terminal.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SpinTheWheelCommandTest {
  private final Array<Entity> entities = new Array<>();

  @BeforeEach
  void beforeEach() {
    EntityService entityService = mock(EntityService.class);
    when(entityService.getEntities()).thenReturn(entities);
    ServiceLocator.registerEntityService(entityService);
  }

  @Test
  void shouldAskThePlayerToSpinTheWheel() {
    entities.add(new Entity());
    Entity player = new Entity().addComponent(new PlayerActions());
    entities.add(player);
    EventListener0 listener = mock(EventListener0.class);
    player.getEvents().addListener("spinTheWheel", listener);

    assertTrue(new SpinTheWheelCommand().action(new ArrayList<>()));
    verify(listener).handle();
  }

  @Test
  void shouldFailWithoutAPlayer() {
    entities.add(new Entity());

    assertFalse(new SpinTheWheelCommand().action(new ArrayList<>()));
  }

  @Test
  void shouldRejectArguments() {
    Entity player = new Entity().addComponent(new PlayerActions());
    entities.add(player);
    EventListener0 listener = mock(EventListener0.class);
    player.getEvents().addListener("spinTheWheel", listener);

    assertFalse(new SpinTheWheelCommand().action(new ArrayList<>(List.of("now"))));
    verify(listener, never()).handle();
  }
}
