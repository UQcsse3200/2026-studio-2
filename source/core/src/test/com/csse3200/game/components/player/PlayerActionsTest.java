package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.level.SlipperyPlatformComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsEngine;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Covers how the player's movement freezes once they've died. */
@ExtendWith(GameExtension.class)
class PlayerActionsTest {
  private GameTime gameTime;

  @BeforeEach
  void beforeEach() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    gameTime = mock(GameTime.class);
    when(gameTime.getTime()).thenReturn(0L);
    ServiceLocator.registerTimeSource(gameTime);
  }

  private Entity createPlayer() {
    Entity player =
        new Entity().addComponent(new PhysicsComponent()).addComponent(new PlayerActions());
    player.create();
    return player;
  }

  /**
   * Drops a static floor just under the player's grounded raycast so {@code checkGrounded()} hits
   * it. The player sits at the origin with a default 1x1 scale, so the ray runs from y=0 to
   * y=-0.15.
   */
  private void createGroundUnderPlayer() {
    createSurfaceUnderPlayer(PhysicsLayer.GROUND);
  }

  /** The same floor, but on the layer an enemy's solid body sits on. */
  private void createEnemyUnderPlayer() {
    createSurfaceUnderPlayer(PhysicsLayer.CHARACTER);
  }

  private void createSurfaceUnderPlayer(short categoryBits) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.StaticBody;
    bodyDef.position.set(0.5f, -0.2f);
    Body ground = ServiceLocator.getPhysicsService().getPhysics().createBody(bodyDef);

    PolygonShape box = new PolygonShape();
    box.setAsBox(1f, 0.1f);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = box;
    fixtureDef.filter.categoryBits = categoryBits;
    ground.createFixture(fixtureDef);
    box.dispose();
  }

  private AtomicInteger countEvent(Entity player, String event) {
    AtomicInteger count = new AtomicInteger();
    player.getEvents().addListener(event, count::incrementAndGet);
    return count;
  }

  @Test
  void shouldAnnounceFallOnlyOncePerDescent() {
    Entity player = createPlayer();
    AtomicInteger falls = countEvent(player, "fallStart");
    player.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(0f, -5f);

    player.update();
    player.update();

    assertEquals(1, falls.get());
  }

  @Test
  void shouldNotAnnounceFallWhileRising() {
    Entity player = createPlayer();
    AtomicInteger falls = countEvent(player, "fallStart");
    player.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(0f, 5f);

    player.update();

    assertEquals(0, falls.get());
  }

  @Test
  void shouldAnnounceLandingAfterAFall() {
    Entity player = createPlayer();
    AtomicInteger landings = countEvent(player, "landed");
    player.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(0f, -5f);
    player.update(); // airborne and falling

    createGroundUnderPlayer();
    player.update();

    assertEquals(1, landings.get());
  }

  @Test
  void shouldNotReportFallingWhileGrappledAndResumeAfterRelease() {
    GrappleComponent grapple = new GrappleComponent();
    Entity player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(grapple)
            .addComponent(new PlayerActions());
    player.create();
    AtomicInteger falls = countEvent(player, "fallStart");
    player.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(0f, -5f);

    player.update();
    assertEquals(1, falls.get(), "descending freely should report a fall");

    // Latch onto a rope. PlayerActions drops the rope at the end of each update because no right
    // mouse button is held in this harness, but updateAirState() runs first and sees it attached.
    BodyDef anchorDef = new BodyDef();
    anchorDef.type = BodyDef.BodyType.StaticBody;
    anchorDef.position.set(0f, 5f);
    Body anchor = ServiceLocator.getPhysicsService().getPhysics().createBody(anchorDef);
    grapple.attachTo(anchor, new Vector2(0f, 5f));
    grapple.update(); // builds the queued joint
    assertTrue(grapple.isAttached());

    player.update();
    assertEquals(1, falls.get(), "swinging is not falling");

    // Back in free air and still descending, so the fall is reported again.
    player.update();
    assertEquals(2, falls.get());
  }

  @Test
  void shouldNotReportAGentleDriftAsFalling() {
    Entity player = createPlayer();
    AtomicInteger falls = countEvent(player, "fallStart");
    // Under FALL_SPEED_THRESHOLD: hang time at the apex and scuffs over seams must not flicker
    // the animation into a fall.
    player.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(0f, -0.5f);

    player.update();

    assertEquals(0, falls.get());
  }

  @Test
  void shouldNotReportFallingWhileDashing() {
    Entity player = createPlayer();
    AtomicInteger falls = countEvent(player, "fallStart");
    player.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(0f, -5f);

    player.getEvents().trigger("dash"); // an air dash pins vertical velocity at zero
    player.update();

    assertEquals(0, falls.get());
  }

  @Test
  void shouldReportLandingOnlyOncePerTouchdown() {
    Entity player = createPlayer();
    AtomicInteger landings = countEvent(player, "landed");
    player.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(0f, -5f);
    player.update(); // airborne

    createGroundUnderPlayer();
    player.update();
    player.update();

    assertEquals(1, landings.get());
  }

  @Test
  void shouldReportALandingWhenAJumpNeverLeavesTheGround() {
    Entity player = createPlayer();
    createGroundUnderPlayer();
    player.update(); // grounded
    AtomicInteger landings = countEvent(player, "landed");

    player.getEvents().trigger("jump");
    when(gameTime.getTime()).thenReturn(100L);
    player.update(); // wind-up elapses, impulse fires, but the body never clears the ground

    when(gameTime.getTime()).thenReturn(250L);
    player.update();
    assertEquals(0, landings.get(), "still inside the liftoff grace window");

    // Jumping straight into a low ceiling would otherwise strand the takeoff pose forever.
    when(gameTime.getTime()).thenReturn(350L);
    player.update();
    assertEquals(1, landings.get());
  }

  @Test
  void shouldCancelAQueuedJumpOnDeathMidWindup() {
    Entity player = createPlayer();
    createGroundUnderPlayer();
    player.update();
    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);

    player.getEvents().trigger("jump");
    player.getEvents().trigger("death"); // dies mid-crouch, before the impulse lands

    when(gameTime.getTime()).thenReturn(1_000L);
    player.update();

    assertEquals(0f, physics.getBody().getLinearVelocity().y);
  }

  @Test
  void shouldTreatAnEnemyUnderfootAsGround() {
    Entity player = createPlayer();
    AtomicInteger landings = countEvent(player, "landed");
    AtomicInteger falls = countEvent(player, "fallStart");
    player.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(0f, -5f);
    player.update(); // airborne and falling
    assertEquals(1, falls.get());

    createEnemyUnderPlayer();
    player.update();

    // Landing on an enemy's head is a landing. Without this the fall loop played forever on top
    // of them, because only GROUND and OBSTACLE counted as something to stand on.
    assertEquals(1, landings.get());
  }

  @Test
  void shouldAllowJumpingOffAnEnemy() {
    Entity player = createPlayer();
    createEnemyUnderPlayer();
    player.update(); // standing on the enemy
    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);

    player.getEvents().trigger("jump");
    when(gameTime.getTime()).thenReturn(1_000L);
    player.update();

    assertTrue(physics.getBody().getLinearVelocity().y > 0f);
  }

  @Test
  void shouldRopeJumpWhenAGroundJumpIsStillWindingUp() {
    GrappleComponent grapple = new GrappleComponent();
    Entity player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(grapple)
            .addComponent(new PlayerActions());
    player.create();
    createGroundUnderPlayer();
    player.update(); // grounded
    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);

    player.getEvents().trigger("jump"); // queues a ground jump, 90ms of crouch to go

    BodyDef anchorDef = new BodyDef();
    anchorDef.type = BodyDef.BodyType.StaticBody;
    anchorDef.position.set(0f, 5f);
    Body anchor = ServiceLocator.getPhysicsService().getPhysics().createBody(anchorDef);
    grapple.attachTo(anchor, new Vector2(0f, 5f));
    grapple.update(); // builds the queued joint

    player.getEvents().trigger("jump"); // rope jump, inside the ground jump's wind-up

    // The rope kick fires rather than being swallowed by the pending ground jump...
    float ropeKickVelocity = physics.getBody().getLinearVelocity().y;
    assertTrue(ropeKickVelocity > 0f, "the rope jump should still kick off the rope");

    // ...and the queued ground impulse is dropped rather than stacking on top of it.
    when(gameTime.getTime()).thenReturn(1_000L);
    player.update();
    assertEquals(ropeKickVelocity, physics.getBody().getLinearVelocity().y, 0.0001f);
  }

  @Test
  void shouldNotAnnounceLandingDuringTheJumpWindup() {
    Entity player = createPlayer();
    createGroundUnderPlayer();
    player.update(); // grounded
    AtomicInteger landings = countEvent(player, "landed");

    player.getEvents().trigger("jump");
    player.update(); // still crouching through the wind-up, feet on the ground

    assertEquals(0, landings.get());
  }

  @Test
  void shouldDelayTheJumpImpulseUntilTheWindupElapses() {
    Entity player = createPlayer();
    createGroundUnderPlayer();
    player.update(); // grounded
    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);

    player.getEvents().trigger("jump");
    player.update();

    // Regression: the jump used to fire an impulse immediately *and* again after the wind-up,
    // which both doubled the jump height and meant the crouch animation played in mid-air.
    assertEquals(0f, physics.getBody().getLinearVelocity().y);

    when(gameTime.getTime()).thenReturn(1_000L);
    player.update();

    assertTrue(physics.getBody().getLinearVelocity().y > 0f);
  }

  @Test
  void shouldZeroHorizontalVelocityOnDeathButKeepVertical() {
    Entity player = createPlayer();
    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
    physics.getBody().setLinearVelocity(3f, -2f);

    player.getEvents().trigger("death");

    assertEquals(0f, physics.getBody().getLinearVelocity().x);
    assertEquals(-2f, physics.getBody().getLinearVelocity().y);
  }

  @Test
  void shouldIgnoreWalkAfterDeath() {
    Entity player = createPlayer();
    player.getEvents().trigger("death");

    player.getEvents().trigger("walk", new com.badlogic.gdx.math.Vector2(1f, 0f));
    player.update(); // Would apply a movement impulse if the death guard were missing.

    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
    assertEquals(0f, physics.getBody().getLinearVelocity().x);
  }

  @Test
  void shouldNotQueueJumpAfterDeath() {
    Entity player = createPlayer();
    player.getEvents().trigger("death");

    player.getEvents().trigger("jump");

    // Advance well past the jump wind-up window and update; a wrongly-queued impulse would apply
    // here.
    when(gameTime.getTime()).thenReturn(10_000L);
    player.update();

    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
    assertEquals(0f, physics.getBody().getLinearVelocity().y);
  }

  @Test
  void shouldDetectSlipperyPlatformWhenGrounded() {
    Entity player = createPlayer();

    Entity platform = new Entity();
    SlipperyPlatformComponent slippery = mock(SlipperyPlatformComponent.class);
    when(slippery.getSlipperiness()).thenReturn(0.1f);
    platform.addComponent(slippery);

    PhysicsService physics = mock(PhysicsService.class);
    ServiceLocator.registerPhysicsService(physics);
    PhysicsEngine engine = mock(PhysicsEngine.class);
    when(physics.getPhysics()).thenReturn(engine);

    // mock raycast
    when(engine.raycast(any(), any(), eq(PhysicsLayer.SOLID), any()))
        .thenAnswer(
            invocation -> {
              RaycastHit hit = invocation.getArgument(3); // mock the out hit parameter

              BodyUserData data =
                  new BodyUserData(); // mock the body user data to be stored in the fixture
              data.entity = platform;

              hit.fixture = mock(Fixture.class);
              Body body = mock(Body.class);
              when(hit.fixture.getBody()).thenReturn(body);
              when(body.getUserData()).thenReturn(data);

              return true; // confirm valid contact
            });

    player.update();
    verify(slippery, atLeastOnce()).getSlipperiness(); // ensure slipperiness is queried
  }
}
