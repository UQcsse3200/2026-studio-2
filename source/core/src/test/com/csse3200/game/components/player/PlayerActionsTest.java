package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.components.level.SlipperyPlatformComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsEngine;
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

  private Body createSurfaceUnderPlayer(short categoryBits) {
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
    return ground;
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

    // Latch onto a rope and stay attached until the release event.
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
    player.getEvents().trigger("grappleRelease");
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
    when(engine.raycast(any(), any(), eq(PhysicsLayer.STANDABLE), any()))
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

  @Test
  void shouldCancelDashAndRestoreGravityOnDeath() {
    Entity player = createPlayer();
    Body body = player.getComponent(PhysicsComponent.class).getBody();
    body.setGravityScale(1.7f);
    player.getEvents().trigger("dash");
    assertEquals(0f, body.getGravityScale());
    player.getEvents().trigger("death");
    player.update();
    assertEquals(1.7f, body.getGravityScale(), 0.001f);
    assertEquals(0f, body.getLinearVelocity().x, 0.001f);
    player.getEvents().trigger("dash");
    assertEquals(0f, body.getLinearVelocity().x, 0.001f);
  }

  @Test
  void shouldRestoreWalkingSpeedAfterRevival() {
    Entity player = createPlayer();
    PlayerActions actions = player.getComponent(PlayerActions.class);
    Body body = player.getComponent(PhysicsComponent.class).getBody();
    actions.walk(Vector2.X);
    player.update();
    float walkingSpeed = body.getLinearVelocity().x;
    actions.sprint();
    player.getEvents().trigger("death");
    player.getEvents().trigger("revive");
    actions.walk(Vector2.X);
    player.update();
    assertEquals(walkingSpeed, body.getLinearVelocity().x, 0.001f);
  }

  @Test
  void shouldKeepFacingWhenVerticalWalkInputArrivesAndDashInThatDirection() {
    Entity player = createPlayer();
    PlayerActions actions = player.getComponent(PlayerActions.class);
    actions.walk(new Vector2(-1f, 0f));
    actions.walk(Vector2.Y);
    assertEquals(-1, actions.getFacingDirection());
    actions.dash();
    assertEquals(-14f, player.getComponent(PhysicsComponent.class).getBody().getLinearVelocity().x);
  }

  @Test
  void shouldApplyAndExpireSpeedPotionWhileWalking() {
    Entity player = createPlayer();
    createGroundUnderPlayer();
    player.update();
    PlayerActions actions = player.getComponent(PlayerActions.class);
    Body body = player.getComponent(PhysicsComponent.class).getBody();
    assertFalse(actions.isSpeedPotionActive());
    actions.walk(Vector2.X);
    player.getEvents().trigger("speedPotionUsed", 0.5f, 1f);
    assertEquals(7.5f, body.getLinearVelocity().x, 0.001f);
    assertTrue(actions.isSpeedPotionActive());
    when(gameTime.getTime()).thenReturn(999L);
    player.update();
    assertTrue(actions.isSpeedPotionActive());
    when(gameTime.getTime()).thenReturn(1000L);
    assertFalse(actions.isSpeedPotionActive());
    player.update();
    player.update();
    assertEquals(5f, body.getLinearVelocity().x, 0.001f);
    assertFalse(actions.isSpeedPotionActive());
  }

  @Test
  void shouldRetainPotionMovementWhenTimeSourceIsTemporarilyUnavailable() {
    Entity player = createPlayer();
    createGroundUnderPlayer();
    player.update();
    PlayerActions actions = player.getComponent(PlayerActions.class);
    actions.walk(Vector2.X);
    player.getEvents().trigger("speedPotionUsed", 1f, 2f);
    ServiceLocator.registerTimeSource(null);
    assertFalse(actions.isSpeedPotionActive());
    player.update();
    assertEquals(10f, player.getComponent(PhysicsComponent.class).getBody().getLinearVelocity().x);
    ServiceLocator.registerTimeSource(gameTime);
    assertTrue(actions.isSpeedPotionActive());
  }

  @Test
  void shouldNotRestartJumpWindupOrAllowAnotherJumpInAir() {
    Entity player = createPlayer();
    createGroundUnderPlayer();
    player.update();
    AtomicInteger jumps = countEvent(player, "jumpStart");
    player.getEvents().trigger("jump");
    when(gameTime.getTime()).thenReturn(80L);
    player.getEvents().trigger("jump");
    when(gameTime.getTime()).thenReturn(90L);
    player.update();
    assertEquals(41f, player.getComponent(PhysicsComponent.class).getBody().getLinearVelocity().y);
    assertEquals(1, jumps.get());
    player.setPosition(0f, 5f);
    player.update();
    player.getEvents().trigger("jump");
    assertEquals(1, jumps.get());
  }

  @Test
  void shouldIgnoreSprintPressAndReleaseAfterDeath() {
    Entity player = createPlayer();
    Body body = player.getComponent(PhysicsComponent.class).getBody();
    player.getEvents().trigger("death");
    player.getEvents().trigger("sprint");
    player.getEvents().trigger("sprintStop");
    assertEquals(0f, body.getLinearVelocity().x);
    assertEquals(1f, body.getGravityScale());
  }

  @Test
  void shouldNotExtendSprintReleaseGraceWhenReleaseRepeats() {
    Entity player = createPlayer();
    PlayerActions actions = player.getComponent(PlayerActions.class);
    AtomicInteger ends = countEvent(player, "sprintEnd");
    actions.stopSprinting();
    assertEquals(0, ends.get());
    actions.sprint();
    actions.stopSprinting();
    when(gameTime.getDeltaTime()).thenReturn(0.08f);
    player.update();
    actions.stopSprinting();
    player.update();
    assertEquals(1, ends.get());
  }

  private Entity createGrapplingPlayer() {
    Entity player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new GrappleComponent())
            .addComponent(new PlayerActions());
    player.create();
    return player;
  }

  private void attachRope(Entity player) {
    BodyDef anchorDef = new BodyDef();
    anchorDef.type = BodyDef.BodyType.StaticBody;
    anchorDef.position.set(0f, 5f);
    Body anchor = ServiceLocator.getPhysicsService().getPhysics().createBody(anchorDef);
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    grapple.attachTo(anchor, new Vector2(0f, 5f));
    grapple.update();
    assertTrue(grapple.isAttached());
  }

  @Test
  void shouldSendWalkDirectionToSwingAndLeaveMomentumWhenWalkingStops() {
    Entity player = createGrapplingPlayer();
    attachRope(player);
    PlayerActions actions = player.getComponent(PlayerActions.class);
    Body body = player.getComponent(PhysicsComponent.class).getBody();
    java.util.List<Float> swings = new java.util.ArrayList<>();
    player.getEvents().addListener("grappleSwing", (Float direction) -> swings.add(direction));
    actions.walk(new Vector2(-1f, 0f));
    actions.update();
    assertEquals(java.util.List.of(-1f), swings);
    body.setLinearVelocity(6f, 2f);
    actions.stopWalking();
    assertEquals(new Vector2(6f, 2f), body.getLinearVelocity());
    actions.sprint();
    actions.stopSprinting();
    when(gameTime.getDeltaTime()).thenReturn(0.13f);
    actions.update();
    assertEquals(new Vector2(6f, 2f), body.getLinearVelocity());
  }

  @Test
  void shouldRestoreGravityWhenJumpingOffRopeAttachedDuringDash() {
    Entity player = createGrapplingPlayer();
    PlayerActions actions = player.getComponent(PlayerActions.class);
    Body body = player.getComponent(PhysicsComponent.class).getBody();
    body.setGravityScale(1.4f);
    actions.dash();
    actions.stopWalking();
    assertEquals(14f, body.getLinearVelocity().x);
    attachRope(player);
    actions.jump();
    assertFalse(player.getComponent(GrappleComponent.class).isAttached());
    assertEquals(1.4f, body.getGravityScale());
    assertEquals(28.7f, body.getLinearVelocity().y, 0.001f);
  }

  @Test
  void shouldSkipRecoverySteeringWhenRopeAttachesAsDashEnds() {
    Entity player = createGrapplingPlayer();
    PlayerActions actions = player.getComponent(PlayerActions.class);
    actions.walk(Vector2.X);
    actions.dash();
    attachRope(player);
    when(gameTime.getDeltaTime()).thenReturn(0.16f);
    actions.update();
    assertEquals(14f, player.getComponent(PhysicsComponent.class).getBody().getLinearVelocity().x);
    assertEquals(1f, player.getComponent(PhysicsComponent.class).getBody().getGravityScale());
  }

  @Test
  void shouldEndSprintWithoutBrakingAnActiveDash() {
    Entity player = createPlayer();
    PlayerActions actions = player.getComponent(PlayerActions.class);
    actions.sprint();
    actions.stopSprinting();
    when(gameTime.getDeltaTime()).thenReturn(0.125f);
    actions.update();
    assertEquals(14f, player.getComponent(PhysicsComponent.class).getBody().getLinearVelocity().x);
    assertEquals(0f, player.getComponent(PhysicsComponent.class).getBody().getGravityScale());
  }

  @Test
  void shouldRefuseSecondAirDashEvenAfterCooldownUntilTouchdown() {
    Entity player = createPlayer();
    PlayerActions actions = player.getComponent(PlayerActions.class);
    AtomicInteger dashes = countEvent(player, "airDashStart");
    actions.dash();
    when(gameTime.getDeltaTime()).thenReturn(1.1f);
    actions.update();
    actions.dash();
    assertEquals(1, dashes.get());
    createGroundUnderPlayer();
    actions.update();
    player.setPosition(0f, 5f);
    actions.update();
    actions.dash();
    assertEquals(2, dashes.get());
  }

  @Test
  void shouldWakeBodyOnlyWhenLedgeDropBegins() {
    Entity player = createPlayer();
    Body body = player.getComponent(PhysicsComponent.class).getBody();
    body.setAwake(false);
    player.getEvents().trigger("updateLedgeDrop", false);
    assertFalse(body.isAwake());
    player.getEvents().trigger("updateLedgeDrop", true);
    assertTrue(body.isAwake());
    assertTrue(player.getComponent(PlayerActions.class).droppingFromLedge);
  }

  @Test
  void shouldUseNormalStoppingTractionForGroundWithoutAnEntityOrSlipperyComponent() {
    Entity player = createPlayer();
    Body floor = createSurfaceUnderPlayer(PhysicsLayer.GROUND);
    BodyUserData data = new BodyUserData();
    floor.setUserData(data);
    player.update();
    Body body = player.getComponent(PhysicsComponent.class).getBody();
    body.setLinearVelocity(5f, 0f);
    player.getEvents().trigger("walkStop");
    assertEquals(0f, body.getLinearVelocity().x);
    data.entity = new Entity();
    player.update();
    body.setLinearVelocity(5f, 0f);
    player.getEvents().trigger("walkStop");
    assertEquals(0f, body.getLinearVelocity().x);
  }

  @Test
  void shouldApplySprintSpeedWhenDashIsUnavailableAndRestoreWalkAfterRelease() {
    Entity player = createPlayer();
    PlayerActions actions = player.getComponent(PlayerActions.class);
    Body body = player.getComponent(PhysicsComponent.class).getBody();
    actions.dash();
    when(gameTime.getDeltaTime()).thenReturn(1.1f);
    actions.update();
    actions.walk(Vector2.X);
    body.setLinearVelocity(0f, 0f);
    actions.sprint();
    assertEquals(0.875f, body.getLinearVelocity().x, 0.001f);
    actions.stopSprinting();
    when(gameTime.getDeltaTime()).thenReturn(0.13f);
    actions.update();
    assertTrue(body.getLinearVelocity().x < 2f, "Release should restore ordinary air steering");
  }

  @Test
  void shouldAcceptLedgeDropBeforePhysicsBodyBecomesAvailable() {
    PhysicsComponent physics = mock(PhysicsComponent.class);
    PlayerActions actions = new PlayerActions();
    Entity player = new Entity().addComponent(physics).addComponent(actions);
    actions.create();
    player.getEvents().trigger("updateLedgeDrop", true);
    assertTrue(actions.droppingFromLedge);
    Body body = mock(Body.class);
    when(physics.getBody()).thenReturn(body);
    player.getEvents().trigger("updateLedgeDrop", true);
    verify(body).setAwake(true);
  }

  @Test
  void shouldDashLeftWhileWalkingLeft() {
    Entity player = createPlayer();
    player.getEvents().trigger("walk", new Vector2(-1f, 0f));
    player.getEvents().trigger("dash");
    assertEquals(-14f, player.getComponent(PhysicsComponent.class).getBody().getLinearVelocity().x);
  }
}
