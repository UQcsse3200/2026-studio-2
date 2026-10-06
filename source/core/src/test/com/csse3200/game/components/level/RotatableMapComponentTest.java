package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class RotatableMapComponentTest {

  @Test
  void shouldReturnRotation() {
    RotatableMapComponent component = new RotatableMapComponent(45f);

    assertEquals(45f, component.getRotation());
  }

  @Test
  void shouldGetPhysicsComponentOnCreate() {
    PhysicsComponent physics = mock(PhysicsComponent.class);

    RotatableMapComponent component = new RotatableMapComponent(90f);

    Entity entity =
        new Entity()
            .addComponent(physics)
            .addComponent(component);

    entity.create();

    assertSame(physics, component.physicsComponent);
  }

  @Test
  void shouldNotInitialiseWhenBodyIsNull() {
    PhysicsComponent physics = mock(PhysicsComponent.class);

    when(physics.getBody()).thenReturn(null);

    RotatableMapComponent component = new RotatableMapComponent(90f);

    Entity entity =
        new Entity()
            .addComponent(physics)
            .addComponent(component);

    entity.create();

    component.update();

    assertFalse(component.initialised);
    verify(physics).getBody();
  }

  @Test
  void shouldInitialiseWhenBodyExists() {
    PhysicsComponent physics = mock(PhysicsComponent.class);
    Body body = mock(Body.class);

    when(physics.getBody()).thenReturn(body);

    RotatableMapComponent component = new RotatableMapComponent(90f);

    Entity entity =
        new Entity()
            .addComponent(physics)
            .addComponent(component);

    entity.create();

    component.update();

    assertTrue(component.initialised);
    verify(physics).getBody();
  }

  @Test
  void shouldOnlyCheckBodyUntilInitialised() {
    PhysicsComponent physics = mock(PhysicsComponent.class);
    Body body = mock(Body.class);

    when(physics.getBody()).thenReturn(body);

    RotatableMapComponent component = new RotatableMapComponent(180f);

    Entity entity =
        new Entity()
            .addComponent(physics)
            .addComponent(component);

    entity.create();

    component.update();
    component.update();
    component.update();

    assertTrue(component.initialised);
    verify(physics, times(1)).getBody();
  }

  @Test
  void shouldRetryInitialisationWhenBodyBecomesAvailable() {
    PhysicsComponent physics = mock(PhysicsComponent.class);
    Body body = mock(Body.class);

    when(physics.getBody())
        .thenReturn(null)
        .thenReturn(body);

    RotatableMapComponent component = new RotatableMapComponent(30f);

    Entity entity =
        new Entity()
            .addComponent(physics)
            .addComponent(component);

    entity.create();

    component.update();

    assertFalse(component.initialised);

    component.update();

    assertTrue(component.initialised);
    verify(physics, times(2)).getBody();
  }
}