package com.csse3200.game.physics;

import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.level.LedgeComponent;
import com.csse3200.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Box2D collision events fire globally on the physics world, not per-object. The contact listener
 * receives these events, finds the entities involved in the collision, and triggers events on them.
 *
 * <p>On contact start: evt = "collisionStart", params = ({@link Fixture} thisFixture, {@link
 * Fixture} otherFixture)
 *
 * <p>On contact end: evt = "collisionEnd", params = ({@link Fixture} thisFixture, {@link Fixture}
 * otherFixture)
 */
public class PhysicsContactListener implements ContactListener {
  private static final Logger logger = LoggerFactory.getLogger(PhysicsContactListener.class);

  @Override
  public void beginContact(Contact contact) {
    triggerEventOn(contact.getFixtureA(), "collisionStart", contact.getFixtureB());
    triggerEventOn(contact.getFixtureB(), "collisionStart", contact.getFixtureA());
  }

  @Override
  public void endContact(Contact contact) {
    triggerEventOn(contact.getFixtureA(), "collisionEnd", contact.getFixtureB());
    triggerEventOn(contact.getFixtureB(), "collisionEnd", contact.getFixtureA());
  }

  @Override
  public void preSolve(Contact contact, Manifold oldManifold) {
    // determine what has made contact
    Fixture fixtureA = contact.getFixtureA();
    Fixture fixtureB = contact.getFixtureB();

    // get BodyUserData for each entity that made contact and ensure they're not null
    BodyUserData dataA = (BodyUserData) fixtureA.getBody().getUserData();
    BodyUserData dataB = (BodyUserData) fixtureB.getBody().getUserData();
    if (dataA == null || dataB == null) {
      return;
    }

    // get entity reference stored in BodyUserData and ensure they're not null
    Entity entityA = dataA.entity;
    Entity entityB = dataB.entity;
    if (entityA == null || entityB == null) {
      return;
    }

    // determine if one of the fixtures involved in the collision is a ledge
    boolean entityALedge = entityA.getComponent(LedgeComponent.class) != null;
    boolean entityBLedge = entityB.getComponent(LedgeComponent.class) != null;
    if (!entityALedge && !entityBLedge) {
      return;
    }

    // if we found a ledge, we need to check if the player is attempting to move through the bottom
    // of the ledge fixture. if so, we disable contact for this collision
    Fixture playerFixture = entityALedge ? fixtureB : fixtureA;

    // determine velocity to ensure the player is moving up at time of collision
    Body playerBody = playerFixture.getBody();
    if (playerBody.getLinearVelocity().y > 0) {
      contact.setEnabled(false);
    }
  }

  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {
    // Nothing to do after resolving contact
  }

  private void triggerEventOn(Fixture fixture, String evt, Fixture otherFixture) {
    BodyUserData userData = (BodyUserData) fixture.getBody().getUserData();
    if (userData != null && userData.entity != null) {
      logger.debug("{} on entity {}", evt, userData.entity);
      userData.entity.getEvents().trigger(evt, fixture, otherFixture);
    }
  }
}
