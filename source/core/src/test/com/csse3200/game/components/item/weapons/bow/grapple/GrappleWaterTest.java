package com.csse3200.game.components.item.weapons.bow.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Covers grappling around the rising water in level 3. Its surface is a moving sensor on the
 * OBSTACLE layer, and touching it respawns the player from inside a collision callback - which used
 * to destroy the rope's joint mid physics step and crash the game.
 */
@ExtendWith(GameExtension.class)
class GrappleWaterTest {
  private World world;
  private Entity player;
  private GrappleComponent grapple;

  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    ServiceLocator.registerTimeSource(mock(GameTime.class));
    ServiceLocator.registerEntityService(mock(EntityService.class));
    world = ServiceLocator.getPhysicsService().getPhysics().getWorld();

    grapple = new GrappleComponent();
    player = new Entity().addComponent(new PhysicsComponent()).addComponent(grapple);
    player.create();
  }

  private Body createBox(
      BodyDef.BodyType type, Vector2 position, float halfWidth, float halfHeight, boolean sensor) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = type;
    bodyDef.position.set(position);
    Body body = world.createBody(bodyDef);
    PolygonShape box = new PolygonShape();
    box.setAsBox(halfWidth, halfHeight);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = box;
    fixtureDef.isSensor = sensor;
    fixtureDef.filter.categoryBits = PhysicsLayer.OBSTACLE;
    body.createFixture(fixtureDef);
    box.dispose();
    return body;
  }

  private void attachRopeAbove() {
    Body anchor = createBox(BodyDef.BodyType.StaticBody, new Vector2(0f, 5f), 0.5f, 0.5f, false);
    grapple.attachTo(anchor, new Vector2(0f, 4.5f));
    grapple.update(); // builds the queued joint
    assertTrue(grapple.isAttached());
  }

  @Test
  void shouldLetGoOfTheRopeSafelyWhenAskedFromInsideThePhysicsStep() {
    attachRopeAbove();
    // Two overlapping boxes well away from the player, so stepping the world reports a contact.
    createBox(BodyDef.BodyType.StaticBody, new Vector2(50f, 0f), 1f, 1f, false);
    createBox(BodyDef.BodyType.DynamicBody, new Vector2(50f, 0.5f), 0.5f, 0.5f, false);
    AtomicInteger releasesMidStep = new AtomicInteger();
    world.setContactListener(
        new ContactListener() {
          @Override
          public void beginContact(Contact contact) {
            // Exactly what touching the water does: a collision handler respawns the player,
            // and respawning lets go of the rope.
            if (world.isLocked()) {
              releasesMidStep.incrementAndGet();
              grapple.release();
            }
          }

          @Override
          public void endContact(Contact contact) {}

          @Override
          public void preSolve(Contact contact, Manifold oldManifold) {}

          @Override
          public void postSolve(Contact contact, ContactImpulse impulse) {}
        });

    // Destroying the joint here used to abort Box2D's native code and take the game down with it.
    world.step(1 / 60f, 6, 2);
    assertEquals(1, releasesMidStep.get(), "the release must have been asked for mid-step");

    grapple.update();

    assertFalse(grapple.isAttached());
    assertEquals(0, world.getJointCount(), "the joint is really gone, not orphaned");
  }

  @Test
  void shouldLetGoImmediatelyOutsideThePhysicsStep() {
    attachRopeAbove();

    grapple.release();

    assertFalse(grapple.isAttached());
    assertEquals(0, world.getJointCount());
  }

  @Test
  void shouldNotHookOntoASensor() {
    Body water = createBox(BodyDef.BodyType.KinematicBody, new Vector2(5f, 0f), 5f, 0.5f, true);

    fireArrowInto(water.getFixtureList().first());
    grapple.update();

    assertFalse(grapple.isAttached(), "the water's surface has nothing to hook onto");
  }

  @Test
  void shouldStillHookOntoSolidTerrainOnTheSameLayer() {
    Body ledge = createBox(BodyDef.BodyType.StaticBody, new Vector2(5f, 0f), 5f, 0.5f, false);

    fireArrowInto(ledge.getFixtureList().first());
    grapple.update();

    assertTrue(grapple.isAttached());
  }

  /** Reports the arrow touching {@code target}, well past the arrow's minimum travel. */
  private void fireArrowInto(Fixture target) {
    Entity arrow = new Entity().addComponent(new GrappleArrowComponent(player));
    arrow.create();
    arrow.setPosition(5f, 0f);
    arrow.getEvents().trigger("collisionStart", (Fixture) null, target);
  }

  @Test
  void shouldNotBendTheRopeAroundASensor() {
    Vector2 anchor = new Vector2(0f, 10f);
    Vector2 playerPoint = new Vector2(0f, 0f);
    createBox(BodyDef.BodyType.KinematicBody, new Vector2(0f, 5f), 2f, 0.5f, true);

    assertTrue(GrappleComponent.traceContacts(world, anchor, playerPoint).isEmpty());
  }

  @Test
  void shouldStillBendTheRopeAroundSolidTerrain() {
    Vector2 anchor = new Vector2(0f, 10f);
    Vector2 playerPoint = new Vector2(0f, 0f);
    createBox(BodyDef.BodyType.StaticBody, new Vector2(0f, 5f), 2f, 0.5f, false);

    assertFalse(GrappleComponent.traceContacts(world, anchor, playerPoint).isEmpty());
  }
}
