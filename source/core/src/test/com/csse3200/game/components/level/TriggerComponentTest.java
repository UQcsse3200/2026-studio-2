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

  /** A created entity with a trigger for the given ids. */
  private static Entity triggerEntity(TriggerComponent component) {
    Entity triggerEntity = new Entity().addComponent(component);
    triggerEntity.create();
    return triggerEntity;
  }

  /** Counts how many ids the trigger activates. */
  private static int[] countActivations(Entity triggerEntity) {
    final int[] activations = {0};
    triggerEntity.getEvents().addListener("activateByKey", (String id) -> activations[0]++);
    return activations;
  }

  private static Entity playerEntity() {
    return new Entity().addComponent(mock(PlayerActions.class));
  }

  private static BodyUserData userDataFor(Entity entity) {
    BodyUserData data = new BodyUserData();
    data.entity = entity;
    return data;
  }

  /** Has something whose body carries the given user data run into the trigger. */
  private static void collideWith(Entity triggerEntity, Object bodyUserData) {
    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);

    when(other.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(bodyUserData);

    triggerEntity.getEvents().trigger("collisionStart", null, other);
  }

  @Test
  void shouldReturnIds() {
    String[] ids = {"door1", "platform1"};
    TriggerComponent component = new TriggerComponent(ids, false);

    assertArrayEquals(ids, component.getIds());
  }

  @Test
  void shouldActivateIdsWhenPlayerCollides() {
    Entity triggerEntity =
        triggerEntity(new TriggerComponent(new String[] {"door1", "platform1"}, false));
    int[] activations = countActivations(triggerEntity);

    collideWith(triggerEntity, userDataFor(playerEntity()));

    assertEquals(2, activations[0]);
  }

  @Test
  void shouldNotActivateWhenBodyHasNoUserData() {
    Entity triggerEntity = triggerEntity(new TriggerComponent(new String[] {"door1"}, false));
    int[] activations = countActivations(triggerEntity);

    collideWith(triggerEntity, null);

    assertEquals(0, activations[0]);
  }

  @Test
  void shouldNotActivateWhenBodyHasNoEntity() {
    Entity triggerEntity = triggerEntity(new TriggerComponent(new String[] {"door1"}, false));
    int[] activations = countActivations(triggerEntity);

    collideWith(triggerEntity, new BodyUserData());

    assertEquals(0, activations[0]);
  }

  @Test
  void shouldNotActivateWhenCollisionIsNotPlayer() {
    Entity triggerEntity = triggerEntity(new TriggerComponent(new String[] {"door1"}, false));
    int[] activations = countActivations(triggerEntity);

    collideWith(triggerEntity, userDataFor(new Entity()));

    assertEquals(0, activations[0]);
  }

  @Test
  void shouldOnlyActivateOnceWhenOneTimeActivation() {
    ColliderComponent collider = mock(ColliderComponent.class);
    TriggerComponent component = new TriggerComponent(new String[] {"door1"}, true);
    Entity triggerEntity = new Entity().addComponent(component).addComponent(collider);
    triggerEntity.create();
    int[] activations = countActivations(triggerEntity);
    BodyUserData player = userDataFor(playerEntity());

    collideWith(triggerEntity, player);
    collideWith(triggerEntity, player);

    assertEquals(1, activations[0]);
    assertTrue(component.activated);
    verify(collider).setEnabled(false);
  }

  @Test
  void shouldHandleNullIds() {
    Entity triggerEntity = triggerEntity(new TriggerComponent(null, false));

    assertDoesNotThrow(() -> collideWith(triggerEntity, userDataFor(playerEntity())));
  }
}
