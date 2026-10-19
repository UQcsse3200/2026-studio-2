package com.csse3200.game.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.level.LedgeComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
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

  // this is a measure of how many pixels on the top half of the sides of the platform the player
  // can collide with for the physics engine to accept that as a valid jump and adjust the player's
  // position to reflect a successful jump
  private static final float platformCollisionLenienceHeight = 1f;
  private static final float collisionTolerance = 0.1f;

  // caches results from preSolve to handle the end of contact updating once a collision is complete
  private PlayerActions playerActions; // stores reference to a playerActions from a contact
  private Fixture ledgeFixture; // stores reference to a ledge fixture that was dropped through

  @Override
  public void beginContact(Contact contact) {
    triggerEventOn(contact.getFixtureA(), "collisionStart", contact.getFixtureB());
    triggerEventOn(contact.getFixtureB(), "collisionStart", contact.getFixtureA());
  }

  @Override
  public void endContact(Contact contact) {
    triggerEventOn(contact.getFixtureA(), "collisionEnd", contact.getFixtureB());
    triggerEventOn(contact.getFixtureB(), "collisionEnd", contact.getFixtureA());

    // if we have cached values, we need to check each contact for the initial contact that
    // triggered the ledge drop to signal to the PlayerActions we completed the ledge drop and
    // delete the cached references
    // TODO: can be refactored perhaps to use the collisionEnd event trigger and respond to it on
    //  the PlayerActions component instead of checking every contact after we move through a ledge
    if (ledgeFixture != null
        && playerActions != null
        && (contact.getFixtureA() == ledgeFixture || contact.getFixtureB() == ledgeFixture)) {

      playerActions.droppingFromLedge = false; // stop player from falling through more ledges
      // clear cache
      playerActions = null;
      ledgeFixture = null;
    }
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

    // we only have presolve work to do if a contact involves a ground component (for ledges or the
    // new movement helper system)
    ColliderComponent entityACollider = entityA.getComponent(ColliderComponent.class);
    ColliderComponent entityBCollider = entityB.getComponent(ColliderComponent.class);
    if (entityACollider != null && entityBCollider != null) {
      // do a bitwise AND to check if any contacts are a part of the SOLID layer and should be
      // validated against our physics checks
      if ((entityACollider.getLayer() & PhysicsLayer.SOLID) != 0
          && (entityBCollider.getLayer() & PhysicsLayer.SOLID) != 0) {
        return;
      }

      // determine if either fixture is a player, early return if not
      boolean playerFixtureA = entityA.getComponent(PlayerActions.class) != null;
      boolean playerFixtureB = entityB.getComponent(PlayerActions.class) != null;
      Fixture playerFixture = playerFixtureA ? fixtureA : playerFixtureB ? fixtureB : null;
      if (playerFixture == null) {
        return;
      }

      Entity player = playerFixtureA ? entityA : entityB;
      PlayerActions actions = player.getComponent(PlayerActions.class);
      Entity ground = playerFixtureA ? entityB : entityA;
      Body playerBody = playerFixture.getBody();
      Body groundBody = playerFixtureA ? fixtureB.getBody() : fixtureA.getBody();

      // check if the player bottom has come into contact with the side of a platform's top
      // LENIENCE number of pixels to correct the player movement
      float leftSideCoord = groundBody.getPosition().x;
      float rightSideCoord = groundBody.getPosition().x + ground.getScale().x;
      float topCoord = groundBody.getPosition().y + ground.getScale().y;

      // calculates the
      float playerXLeft = playerBody.getPosition().x;
      float playerXRight = playerBody.getPosition().x + player.getScale().x;

      // determine if a valid side contact was made and let the player know it's new position
      float diff = topCoord - playerBody.getPosition().y;
      if (diff > 0
          && diff <= platformCollisionLenienceHeight + collisionTolerance
          && playerBody.getLinearVelocity().y < 0) {
        if (Math.abs(playerXRight - leftSideCoord) <= collisionTolerance) {
          player
              .getEvents()
              .trigger("physicsPositionRequest", new Vector2(playerXLeft + 0.1f, topCoord + 0.1f));
        } else if (Math.abs(playerXLeft - rightSideCoord) <= collisionTolerance) {
          player
              .getEvents()
              .trigger("physicsPositionRequest", new Vector2(playerXLeft - 0.1f, topCoord + 0.1f));
        }
      }

      // determine if one of the fixtures involved in the collision is a ledge
      boolean entityALedge = entityA.getComponent(LedgeComponent.class) != null;
      boolean entityBLedge = entityB.getComponent(LedgeComponent.class) != null;
      if (entityALedge || entityBLedge) {
        // determine velocity to ensure the player is moving up at time of collision
        if (playerBody.getLinearVelocity().y > 0) {
          contact.setEnabled(false);
          return;
        }

        // resolve any ledge dropping requests active on the player actions component
        if (actions != null && actions.droppingFromLedge) {
          playerActions = actions;
          ledgeFixture = entityALedge ? fixtureA : fixtureB;
          contact.setEnabled(false);
        }
      }
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
