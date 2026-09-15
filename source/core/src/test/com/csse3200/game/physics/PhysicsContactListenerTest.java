package com.csse3200.game.physics;

import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.level.LedgeComponent;
import com.csse3200.game.components.level.PlatformGrappleComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener2;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PhysicsContactListenerTest {
  @BeforeEach
  void beforeEach() {
    // Set up services and time
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(0.02f);
    ServiceLocator.registerTimeSource(gameTime);
    ServiceLocator.registerPhysicsService(new PhysicsService());
  }

  @Test
  void shouldTriggerCollisionStart() {
    // Set up colliding entities
    Entity entity1 = createPhysicsEntity();
    Entity entity2 = createPhysicsEntity();
    entity1.setPosition(0f, 0f);
    entity2.setPosition(0f, 0f);
    Fixture fixture1 = entity1.getComponent(ColliderComponent.class).getFixture();
    Fixture fixture2 = entity2.getComponent(ColliderComponent.class).getFixture();

    // Register collision callbacks
    EventListener2<Fixture, Fixture> callback1 = mock(EventListener2.class);
    EventListener2<Fixture, Fixture> callback2 = mock(EventListener2.class);
    entity1.getEvents().addListener("collisionStart", callback1);
    entity2.getEvents().addListener("collisionStart", callback2);

    // Trigger collisions
    ServiceLocator.getPhysicsService().getPhysics().update();

    // Verify callbacks invoked correctly
    verify(callback1).handle(fixture1, fixture2);
    verify(callback2).handle(fixture2, fixture1);
  }

  @Test
  void shouldTriggerCollisionEnd() {
    // Set up colliding entities
    Entity entity1 = createPhysicsEntity();
    Entity entity2 = createPhysicsEntity();
    entity1.setPosition(0f, 0f);
    entity2.setPosition(0f, 0f);
    Fixture fixture1 = entity1.getComponent(ColliderComponent.class).getFixture();
    Fixture fixture2 = entity2.getComponent(ColliderComponent.class).getFixture();

    // Register end collision callbacks
    EventListener2<Fixture, Fixture> endCallback1 = mock(EventListener2.class);
    EventListener2<Fixture, Fixture> endCallback2 = mock(EventListener2.class);
    entity1.getEvents().addListener("collisionEnd", endCallback1);
    entity2.getEvents().addListener("collisionEnd", endCallback2);

    ServiceLocator.getPhysicsService().getPhysics().update();
    verifyNoInteractions(endCallback1);
    verifyNoInteractions(endCallback2);

    entity1.setPosition(10f, 10f);
    entity2.setPosition(-10f, -10f);
    ServiceLocator.getPhysicsService().getPhysics().update();
    verify(endCallback1).handle(fixture1, fixture2);
    verify(endCallback2).handle(fixture2, fixture1);
  }

  @Test
  void shouldDisableCollisionBetweenPlayerAndLedge() {
    Entity entity1 =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new PlayerActions());

    Entity entity2 =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new LedgeComponent());

    entity1.create();
    entity2.create();

    entity1.setPosition(0f, 0f);
    entity2.setPosition(0f, 0f);

    Fixture fixture1 = entity1.getComponent(ColliderComponent.class).getFixture();
    Fixture fixture2 = entity2.getComponent(ColliderComponent.class).getFixture();

    fixture1.getBody().setLinearVelocity(new Vector2(0f, 1f));

    Contact mockContact = mock(Contact.class);
    Manifold mockManifold = mock(Manifold.class);

    when(mockContact.getFixtureA()).thenReturn(fixture1);
    when(mockContact.getFixtureB()).thenReturn(fixture2);

    PhysicsContactListener listener = new PhysicsContactListener();
    listener.preSolve(mockContact, mockManifold);
    verify(mockContact).setEnabled(false);
  }

  @Test
  void shouldNotDisableCollisionBetweenPlayerAndLedge() {
    Entity entity1 =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new PlayerActions());

    Entity entity2 =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new LedgeComponent());

    entity1.create();
    entity2.create();

    entity1.setPosition(0f, 0f);
    entity2.setPosition(0f, 0f);

    Fixture fixture1 = entity1.getComponent(ColliderComponent.class).getFixture();
    Fixture fixture2 = entity2.getComponent(ColliderComponent.class).getFixture();

    fixture1.getBody().setLinearVelocity(new Vector2(0f, -1f));

    Contact mockContact = mock(Contact.class);
    Manifold mockManifold = mock(Manifold.class);

    when(mockContact.getFixtureA()).thenReturn(fixture1);
    when(mockContact.getFixtureB()).thenReturn(fixture2);

    PhysicsContactListener listener = new PhysicsContactListener();
    listener.preSolve(mockContact, mockManifold);
    verify(mockContact, never()).setEnabled(false);
  }

  @Test
  void shouldEnableCollisionBetweenPlayerAndEntity() {
    Entity entity1 =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new PlayerActions());

    Entity entity2 =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new PlatformGrappleComponent(0));

    entity1.create();
    entity2.create();

    entity1.setPosition(0f, 0f);
    entity2.setPosition(0f, 0f);

    Fixture fixture1 = entity1.getComponent(ColliderComponent.class).getFixture();
    Fixture fixture2 = entity2.getComponent(ColliderComponent.class).getFixture();

    fixture1.getBody().setLinearVelocity(new Vector2(0f, 1f));

    Contact mockContact = mock(Contact.class);
    Manifold mockManifold = mock(Manifold.class);

    when(mockContact.getFixtureA()).thenReturn(fixture1);
    when(mockContact.getFixtureB()).thenReturn(fixture2);

    PhysicsContactListener listener = new PhysicsContactListener();
    listener.preSolve(mockContact, mockManifold);
    verify(mockContact, never()).setEnabled(false);
  }

  Entity createPhysicsEntity() {
    Entity entity =
        new Entity().addComponent(new PhysicsComponent()).addComponent(new ColliderComponent());
    entity.create();
    return entity;
  }

  Fixture createFixture(PhysicsEngine engine) {
    Body body = engine.createBody(new BodyDef());
    return body.createFixture(new FixtureDef());
  }
}
