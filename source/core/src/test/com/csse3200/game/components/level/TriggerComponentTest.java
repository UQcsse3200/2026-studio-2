package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.ColliderComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TriggerComponentTest {

  @Test
  void shouldReturnIds() {
    String[] ids = {"door1", "platform1"};
    TriggerComponent component = new TriggerComponent(ids, false);

    assertArrayEquals(ids, component.getIds());
  }

  @Test
  void shouldActivateIdsWhenPlayerCollides() {
    TriggerComponent component =
        new TriggerComponent(new String[] {"door1", "platform1"}, false);

    Entity triggerEntity = new Entity().addComponent(component);
    triggerEntity.create();

    final int[] activations = {0};

    triggerEntity
        .getEvents()
        .addListener("activateByKey", (String id) -> activations[0]++);

    Entity player = new Entity().addComponent(mock(PlayerActions.class));

    BodyUserData data = new BodyUserData();
    data.entity = player;

    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);

    triggerEntity.getEvents().trigger("collisionStart", null, other);

    assertEquals(2, activations[0]);
  }

  @Test
  void shouldNotActivateWhenBodyHasNoUserData() {
    TriggerComponent component =
        new TriggerComponent(new String[] {"door1"}, false);

    Entity triggerEntity = new Entity().addComponent(component);
    triggerEntity.create();

    final int[] activations = {0};

    triggerEntity
        .getEvents()
        .addListener("activateByKey", (String id) -> activations[0]++);

    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(null);

    triggerEntity.getEvents().trigger("collisionStart", null, other);

    assertEquals(0, activations[0]);
  }

  @Test
  void shouldNotActivateWhenBodyHasNoEntity() {
    TriggerComponent component =
        new TriggerComponent(new String[] {"door1"}, false);

    Entity triggerEntity = new Entity().addComponent(component);
    triggerEntity.create();

    final int[] activations = {0};

    triggerEntity
        .getEvents()
        .addListener("activateByKey", (String id) -> activations[0]++);

    BodyUserData data = new BodyUserData();

    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);

    triggerEntity.getEvents().trigger("collisionStart", null, other);

    assertEquals(0, activations[0]);
  }

  @Test
  void shouldNotActivateWhenCollisionIsNotPlayer() {
    TriggerComponent component =
        new TriggerComponent(new String[] {"door1"}, false);

    Entity triggerEntity = new Entity().addComponent(component);
    triggerEntity.create();

    final int[] activations = {0};

    triggerEntity
        .getEvents()
        .addListener("activateByKey", (String id) -> activations[0]++);

    Entity nonPlayer = new Entity();

    BodyUserData data = new BodyUserData();
    data.entity = nonPlayer;

    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);

    triggerEntity.getEvents().trigger("collisionStart", null, other);

    assertEquals(0, activations[0]);
  }

  @Test
  void shouldOnlyActivateOnceWhenOneTimeActivation() {
    ColliderComponent collider = mock(ColliderComponent.class);

    TriggerComponent component =
        new TriggerComponent(new String[] {"door1"}, true);

    Entity triggerEntity =
        new Entity()
            .addComponent(component)
            .addComponent(collider);

    triggerEntity.create();

    final int[] activations = {0};

    triggerEntity
        .getEvents()
        .addListener("activateByKey", (String id) -> activations[0]++);

    Entity player = new Entity().addComponent(mock(PlayerActions.class));

    BodyUserData data = new BodyUserData();
    data.entity = player;

    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);

    triggerEntity.getEvents().trigger("collisionStart", null, other);
    triggerEntity.getEvents().trigger("collisionStart", null, other);

    assertEquals(1, activations[0]);
    assertTrue(component.activated);
    verify(collider).setEnabled(false);
  }

  @Test
  void shouldHandleNullIds() {
    TriggerComponent component = new TriggerComponent(null, false);

    Entity triggerEntity = new Entity().addComponent(component);
    triggerEntity.create();

    Entity player = new Entity().addComponent(mock(PlayerActions.class));

    BodyUserData data = new BodyUserData();
    data.entity = player;

    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);

    assertDoesNotThrow(
        () -> triggerEntity.getEvents().trigger("collisionStart", null, other));
  }
}