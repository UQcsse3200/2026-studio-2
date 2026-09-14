package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ActivatableComponentTest {
  @Test
  void testValidIds() {
    ActivatableComponent activatable1 = new ActivatableComponent(true, new String[] {"test"});
    assertFalse(activatable1.isActive());

    ActivatableComponent activatable2 = new ActivatableComponent(false, new String[] {"test"});
    assertFalse(activatable2.isActive());

    ActivatableComponent activatable3 = new ActivatableComponent(true, new String[] {});
    assertTrue(activatable3.isActive());

    ActivatableComponent activatable4 = new ActivatableComponent(false, new String[] {});
    assertFalse(activatable4.isActive());
  }

  @Test
  void testInvalidIds() {
    assertThrowsExactly(IllegalArgumentException.class, () -> new ActivatableComponent(true, null));
  }

  @Test
  void shouldActivateComponent() {
    Entity entity = mock(Entity.class);
    EventHandler events = mock(EventHandler.class);
    when(entity.getEvents()).thenReturn(events);

    ActivatableComponent activatable = new ActivatableComponent(false, new String[] {"test"});
    activatable.setEntity(entity);
    activatable.setActive(true);
    assertTrue(activatable.isActive());
  }

  @Test
  void shouldDeactivateComponent() {
    Entity entity = mock(Entity.class);
    EventHandler events = mock(EventHandler.class);
    when(entity.getEvents()).thenReturn(events);

    ActivatableComponent activatable = new ActivatableComponent(true, new String[] {"test"});
    activatable.setEntity(entity);
    activatable.setActive(false);
    assertFalse(activatable.isActive());
  }

  @Test
  void shouldNotActivateUnactivatableComponent() {
    Entity entity = mock(Entity.class);
    EventHandler events = mock(EventHandler.class);
    when(entity.getEvents()).thenReturn(events);

    ActivatableComponent activatable = new ActivatableComponent(false, new String[] {});
    activatable.setEntity(entity);
    activatable.setActive(true);
    assertFalse(activatable.isActive());
  }
}
