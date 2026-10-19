package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.item.weapons.bow.BowCharge;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

@ExtendWith(GameExtension.class)
class GrappleComponentTest {
  // Must match PhysicsComponent's private GROUND_FRICTION default.
  private static final float ORIGINAL_DAMPING = 2f;
  // Must match GrappleComponent's private SWING_DAMPING.
  private static final float SWING_DAMPING = 0.5f;

  private PhysicsService physicsService;
  private GameTime gameTime;

  @BeforeEach
  void beforeEach() {
    gameTime = mock(GameTime.class);
    ServiceLocator.registerTimeSource(gameTime);
    physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);
  }

  private Entity createAttachedPlayer() {
    Entity player =
        new Entity().addComponent(new PhysicsComponent()).addComponent(new GrappleComponent());
    player.create();
    return player;
  }

  private Body createAnchorBody() {
    BodyDef anchorDef = new BodyDef();
    anchorDef.type = BodyDef.BodyType.StaticBody;
    Body anchor = physicsService.getPhysics().createBody(anchorDef);
    anchor.setTransform(5f, 5f, 0f);
    return anchor;
  }

  @Test
  void shouldStartDetached() {
    GrappleComponent grapple = new GrappleComponent();
    assertFalse(grapple.isAttached());
    assertNull(grapple.getAnchorPoint());
  }

  @Test
  void shouldIgnoreReleaseWhenNotAttached() {
    GrappleComponent grapple = new GrappleComponent();
    // Would throw if it tried to destroy a null joint
    grapple.release();
    assertFalse(grapple.isAttached());
  }

  @Test
  void shouldIgnoreSwingWhenNotAttached() {
    GrappleComponent grapple = new GrappleComponent();
    // Would throw on the null body if the guard were missing
    assertDoesNotThrow(() -> grapple.swing(1f));
    assertDoesNotThrow(() -> grapple.swing(-1f));
    assertDoesNotThrow(() -> grapple.swing(0f));
    assertFalse(grapple.isAttached());
  }

  @Test
  void shouldQueueAttachmentWithoutBuildingJoint() {
    GrappleComponent grapple = new GrappleComponent();
    // Queuing only stores the point, the joint waits for update()
    grapple.attachTo(null, new Vector2(3f, 4f));
    assertFalse(grapple.isAttached());
  }

  @Test
  void shouldNotFireWithoutDirection() {
    Entity player = new Entity().addComponent(new GrappleComponent());
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);

    // Would reach ProjectileFactory and fail without a registered service
    try (var factory = mockStatic(ProjectileFactory.class)) {
      grapple.fire(null);
      grapple.fire(Vector2.Zero.cpy());
      factory.verifyNoInteractions();
    }
  }

  @Test
  void shouldCopyAnchorPointOnRead() {
    GrappleComponent grapple = new GrappleComponent();
    assertNull(grapple.getAnchorPoint());
  }

  @Test
  void shouldRestoreOriginalDampingAfterRelease() {
    Entity player = createAttachedPlayer();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    PhysicsComponent physicsComponent = player.getComponent(PhysicsComponent.class);
    Body anchor = createAnchorBody();

    assertEquals(ORIGINAL_DAMPING, physicsComponent.getBody().getLinearDamping());

    grapple.attachTo(anchor, new Vector2(5f, 6f));
    grapple.update(); // builds the queued joint
    assertTrue(grapple.isAttached());
    assertEquals(SWING_DAMPING, physicsComponent.getBody().getLinearDamping());

    grapple.release();

    // Regression: this used to be hardcoded to 0f and never recovered, permanently increasing
    // jump height (less drag decays the jump impulse) after the first grapple use.
    assertEquals(ORIGINAL_DAMPING, physicsComponent.getBody().getLinearDamping());
  }

  @Test
  void shouldRestoreDampingAfterRepeatedAttachReleaseCycles() {
    Entity player = createAttachedPlayer();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    PhysicsComponent physicsComponent = player.getComponent(PhysicsComponent.class);

    for (int i = 0; i < 3; i++) {
      Body anchor = createAnchorBody();
      grapple.attachTo(anchor, new Vector2(5f, 6f));
      grapple.update();
      assertEquals(SWING_DAMPING, physicsComponent.getBody().getLinearDamping());

      grapple.release();
      assertEquals(
          ORIGINAL_DAMPING,
          physicsComponent.getBody().getLinearDamping(),
          "Damping should not drift after cycle " + i);
    }
  }

  @Test
  void shouldReleaseAndResetCooldownOnRespawn() {
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());
    Entity player = createAttachedPlayer();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    PhysicsComponent physicsComponent = player.getComponent(PhysicsComponent.class);
    Body anchor = createAnchorBody();

    grapple.attachTo(anchor, new Vector2(5f, 6f));
    grapple.update();
    assertTrue(grapple.isAttached());

    // A player who fell to their death right after firing could otherwise still land the rope on
    // whatever they were aiming at before the teleport back to the checkpoint.
    player.getEvents().trigger("respawnAtCheckpoint");

    assertFalse(grapple.isAttached());
    assertEquals(ORIGINAL_DAMPING, physicsComponent.getBody().getLinearDamping());

    // The cooldown is cleared too, so respawning doesn't leave the player unable to fire again.
    grapple.fire(Vector2.X.cpy());
  }

  @Test
  void shouldCancelInFlightArrowOnRespawn() {
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());
    Entity player =
        new Entity().addComponent(new PhysicsComponent()).addComponent(new GrappleComponent());
    ServiceLocator.getEntityService().register(player);
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);

    grapple.fire(Vector2.X.cpy());
    int entitiesBeforeRespawn = ServiceLocator.getEntityService().getEntities().size;
    assertTrue(entitiesBeforeRespawn > 1); // the player plus the in-flight grapple arrow

    player.getEvents().trigger("respawnAtCheckpoint");
    ServiceLocator.getEntityService().update(); // processes the scheduled removal

    assertEquals(1, ServiceLocator.getEntityService().getEntities().size);
  }

  @Test
  void shouldChargeOnHoldAndFireOnRelease() {
    ServiceLocator.registerEntityService(new EntityService());
    Entity player =
        new Entity().addComponent(new PhysicsComponent()).addComponent(new GrappleComponent());
    ServiceLocator.getEntityService().register(player);
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);

    grapple.startCharge(Vector2.X.cpy());
    assertTrue(grapple.isCharging());
    assertFalse(grapple.isOnCooldown());

    try (MockedStatic<ProjectileFactory> factory = mockStatic(ProjectileFactory.class)) {
      factory
          .when(() -> ProjectileFactory.createGrappleArrow(eq(player), any(), any(), anyFloat()))
          .thenReturn(new Entity());
      grapple.releaseCharge(Vector2.X.cpy());
      factory.verify(
          () -> ProjectileFactory.createGrappleArrow(eq(player), any(), any(), anyFloat()));
    }

    assertFalse(grapple.isCharging());
    assertTrue(grapple.isOnCooldown());
  }

  @Test
  void shouldClearCooldownOnceTheGrappleActivates() {
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());
    Entity player =
        new Entity().addComponent(new PhysicsComponent()).addComponent(new GrappleComponent());
    ServiceLocator.getEntityService().register(player);
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    Body anchor = createAnchorBody();

    grapple.fire(Vector2.X.cpy());
    assertTrue(grapple.isOnCooldown());

    grapple.attachTo(anchor, new Vector2(5f, 6f));

    // A successful grapple should let you detach and fire again immediately to chain swings - the
    // cooldown only exists to stop a missed shot being spammed for free.
    assertFalse(grapple.isOnCooldown());
  }

  /** A player hanging 5 units directly above an anchor, with the rope just taut. */
  private GrappleComponent attachAboveAnchor(Entity player) {
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    Body anchor = createAnchorBody();
    anchor.setTransform(0f, 0f, 0f);
    player.setPosition(0f, 5f);
    grapple.attachTo(anchor, new Vector2(0f, 0f));
    grapple.update();
    assertTrue(grapple.isAttached());
    return grapple;
  }

  @Test
  void shouldLetThePlayerFallTowardAnAnchorBelowThemInsteadOfSwingingRoundIt() {
    when(gameTime.getDeltaTime()).thenReturn(0.016f);
    Entity player = createAttachedPlayer();
    Body playerBody = player.getComponent(PhysicsComponent.class).getBody();
    attachAboveAnchor(player);

    for (int i = 0; i < 10; i++) {
      physicsService.getPhysics().update();
    }

    // A rope only stops you getting further away, so with the anchor below you it goes slack and
    // you simply fall toward it. A rigid rod holds you at 5 units and forces you round in an arc.
    assertTrue(playerBody.getPosition().y < 4.5f, "fell to y=" + playerBody.getPosition().y);
  }

  @Test
  void shouldStillStopThePlayerGettingFurtherThanTheRopeAllows() {
    when(gameTime.getDeltaTime()).thenReturn(0.016f);
    Entity player = createAttachedPlayer();
    Body playerBody = player.getComponent(PhysicsComponent.class).getBody();
    GrappleComponent grapple = attachAboveAnchor(player);

    // Hurled straight up and away from the anchor, they must be hauled back by the taut rope.
    playerBody.setLinearVelocity(0f, 30f);
    for (int i = 0; i < 30; i++) {
      physicsService.getPhysics().update();
    }

    assertTrue(grapple.isAttached());
    assertTrue(playerBody.getPosition().len() < 5.1f, "reached " + playerBody.getPosition());
  }

  @Test
  void shouldPushRightWhenRightIsHeldEvenWithTheAnchorBelow() {
    when(gameTime.getDeltaTime()).thenReturn(0.016f);
    Entity player = createAttachedPlayer();
    Body playerBody = player.getComponent(PhysicsComponent.class).getBody();
    GrappleComponent grapple = attachAboveAnchor(player);

    grapple.swing(1f);
    physicsService.getPhysics().update();

    // Rotating the anchor-to-player vector a fixed 90 degrees points the push the opposite way
    // once the player is above the anchor, so holding right used to shove them left.
    assertTrue(playerBody.getLinearVelocity().x > 0.05f, "vx=" + playerBody.getLinearVelocity().x);
  }

  @Test
  void shouldPushLeftWhenLeftIsHeldEvenWithTheAnchorBelow() {
    when(gameTime.getDeltaTime()).thenReturn(0.016f);
    Entity player = createAttachedPlayer();
    Body playerBody = player.getComponent(PhysicsComponent.class).getBody();
    GrappleComponent grapple = attachAboveAnchor(player);

    grapple.swing(-1f);
    physicsService.getPhysics().update();

    assertTrue(playerBody.getLinearVelocity().x < -0.05f, "vx=" + playerBody.getLinearVelocity().x);
  }

  @Test
  void shouldKeepPushingTheWayTheKeyIsHeldWhileHangingBelowTheAnchor() {
    when(gameTime.getDeltaTime()).thenReturn(0.016f);
    Entity player = createAttachedPlayer();
    Body playerBody = player.getComponent(PhysicsComponent.class).getBody();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    Body anchor = createAnchorBody();
    anchor.setTransform(0f, 0f, 0f);
    player.setPosition(0f, -5f);
    grapple.attachTo(anchor, new Vector2(0f, 0f));
    grapple.update();

    grapple.swing(1f);
    physicsService.getPhysics().update();
    float afterRight = playerBody.getLinearVelocity().x;
    assertTrue(afterRight > 0.05f, "vx=" + afterRight);

    grapple.swing(-1f);
    physicsService.getPhysics().update();
    assertTrue(playerBody.getLinearVelocity().x < afterRight);
  }

  @Test
  void shouldIgnoreChargeStartWithoutDirectionOrWhileAttachedOrOnCooldown() {
    Entity player = createAttachedPlayer();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    Body anchor = createAnchorBody();

    grapple.startCharge(null);
    assertFalse(grapple.isCharging());
    grapple.startCharge(Vector2.Zero.cpy());
    assertFalse(grapple.isCharging());

    grapple.attachTo(anchor, new Vector2(5f, 6f));
    grapple.update();
    grapple.startCharge(Vector2.X.cpy());
    assertFalse(grapple.isCharging());

    grapple.release();
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());
    grapple.fire(Vector2.X.cpy());
    grapple.startCharge(Vector2.X.cpy());
    assertFalse(grapple.isCharging());
  }

  @Test
  void shouldIgnoreChargeReleaseWhenNothingWasCharging() {
    GrappleComponent grapple = new GrappleComponent();
    // Would throw trying to fire without a registered EntityService, if this weren't a no-op
    grapple.releaseCharge(Vector2.X.cpy());
    assertFalse(grapple.isCharging());
  }

  @Test
  void shouldChargeSpeedLikeTheBowAndClampAtFullDraw() {
    GrappleComponent grapple =
        new Entity().addComponent(new GrappleComponent()).getComponent(GrappleComponent.class);

    // Not charging: a release would just be a normal-speed shot.
    assertEquals(1f, grapple.currentSpeedMultiplier(), 0.001f);

    when(gameTime.getTime()).thenReturn(0L);
    grapple.startCharge(Vector2.X.cpy());
    // A tap starts out weak, exactly as the bow's does.
    assertEquals(BowCharge.MIN_SPEED_FACTOR, grapple.currentSpeedMultiplier(), 0.001f);

    // Halfway through the 1.5s draw, halfway between the minimum and full-draw factors.
    when(gameTime.getTime()).thenReturn(750L);
    float halfway = (BowCharge.MIN_SPEED_FACTOR + BowCharge.MAX_SPEED_FACTOR) / 2f;
    assertEquals(halfway, grapple.currentSpeedMultiplier(), 0.001f);

    // Held well past a full draw - clamps there rather than overshooting.
    when(gameTime.getTime()).thenReturn(10_000L);
    assertEquals(BowCharge.MAX_SPEED_FACTOR, grapple.currentSpeedMultiplier(), 0.001f);
  }

  @Test
  void shouldFireWithScaledSpeedOnReleaseAfterPartialCharge() {
    ServiceLocator.registerEntityService(new EntityService());
    Entity player =
        new Entity().addComponent(new PhysicsComponent()).addComponent(new GrappleComponent());
    ServiceLocator.getEntityService().register(player);
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);

    when(gameTime.getTime()).thenReturn(0L);
    grapple.startCharge(Vector2.X.cpy());

    when(gameTime.getTime()).thenReturn(750L);
    float expected = BowCharge.speedMultiplier(750L);
    try (MockedStatic<ProjectileFactory> factory = mockStatic(ProjectileFactory.class)) {
      factory
          .when(() -> ProjectileFactory.createGrappleArrow(eq(player), any(), any(), eq(expected)))
          .thenReturn(new Entity());
      grapple.releaseCharge(Vector2.X.cpy());
      factory.verify(
          () -> ProjectileFactory.createGrappleArrow(eq(player), any(), any(), eq(expected)));
    }

    assertFalse(grapple.isCharging());
    assertTrue(grapple.isOnCooldown());
  }

  @Test
  void shouldCancelChargeWithoutFiringOnDeath() {
    Entity player = new Entity().addComponent(new GrappleComponent());
    player.create();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);

    grapple.startCharge(Vector2.X.cpy());
    assertTrue(grapple.isCharging());

    player.getEvents().trigger("death");
    assertFalse(grapple.isCharging());
  }

  @Test
  void shouldCancelChargeOnRespawn() {
    Entity player = new Entity().addComponent(new GrappleComponent());
    player.create();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);

    grapple.startCharge(Vector2.X.cpy());
    assertTrue(grapple.isCharging());

    player.getEvents().trigger("respawnAtCheckpoint");

    assertFalse(grapple.isCharging());
  }

  @Test
  void shouldRestoreDampingWhenAnchorBodyIsDestroyed() {
    Entity player = createAttachedPlayer();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    PhysicsComponent physicsComponent = player.getComponent(PhysicsComponent.class);
    Body anchor = createAnchorBody();

    grapple.attachTo(anchor, new Vector2(5f, 6f));
    grapple.update(); // builds the joint
    assertTrue(grapple.isAttached());
    assertEquals(SWING_DAMPING, physicsComponent.getBody().getLinearDamping());

    // Box2D destroys any joints attached to a body when that body is destroyed, without going
    // through GrappleComponent.release() - this is how a grappled enemy dying mid-swing behaves.
    physicsService.getPhysics().getWorld().destroyBody(anchor);
    grapple.update(); // notices the joint is gone and calls forgetJoint()

    assertFalse(grapple.isAttached());
    assertEquals(ORIGINAL_DAMPING, physicsComponent.getBody().getLinearDamping());
  }

  @Test
  void shouldRebuildJointAtOriginalAnchorAfterUnwrappingLastContact() {
    Entity player = createAttachedPlayer();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    Body playerBody = player.getComponent(PhysicsComponent.class).getBody();
    playerBody.setTransform(0f, -5f, 0f);
    Body anchor = createAnchorBody();
    anchor.setTransform(10f, 3f, 0f);
    Vector2 anchorPoint = new Vector2(10f, 3f);
    grapple.attachTo(anchor, anchorPoint);
    grapple.update();
    float initialLength = grapple.getRopeLength();

    BodyDef obstacleDef = new BodyDef();
    obstacleDef.position.set(5f, 0f);
    Body obstacle = physicsService.getPhysics().createBody(obstacleDef);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(1f, 2f);
    Fixture fixture = obstacle.createFixture(shape, 0f);
    shape.dispose();
    Filter filter = fixture.getFilterData();
    filter.categoryBits = PhysicsLayer.SOLID;
    fixture.setFilterData(filter);

    // The shorter player-to-anchor distance leaves room for bends around the obstacle.
    playerBody.setTransform(0f, 0f, 0f);
    grapple.update();
    assertTrue(grapple.getRopePath().size() > 2);
    Array<Joint> joints = new Array<>();
    physicsService.getPhysics().getWorld().getJoints(joints);
    assertEquals(1, joints.size);
    assertEquals(obstacle, joints.first().getBodyA());

    // Crossing above the obstacle removes the final bend and restores the original pivot.
    playerBody.setTransform(0f, 5f, 0f);
    grapple.update();
    assertTrue(grapple.isAttached());
    assertEquals(2, grapple.getRopePath().size());
    assertTrue(anchorPoint.epsilonEquals(grapple.getAnchorPoint(), 0.001f));
    assertEquals(initialLength, grapple.getRopeLength(), 0.001f);
    joints.clear();
    physicsService.getPhysics().getWorld().getJoints(joints);
    assertEquals(1, joints.size);
    assertEquals(anchor, joints.first().getBodyA());
    assertEquals(initialLength, ((RopeJoint) joints.first()).getMaxLength(), 0.001f);
  }

  @Test
  void shouldShortenRopeWhileClimbing() {
    when(gameTime.getDeltaTime()).thenReturn(0.5f);
    Entity player = createAttachedPlayer();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    Body anchor = createAnchorBody();

    grapple.attachTo(anchor, new Vector2(5f, 6f));
    grapple.update();
    float initialLength = grapple.getRopeLength();

    player.getEvents().trigger("grappleClimbStart");
    grapple.update();

    assertTrue(grapple.getRopeLength() < initialLength);
  }

  @Test
  void shouldNotQueueClimbWhilePlayerIsBlocked() {
    when(gameTime.getDeltaTime()).thenReturn(0.5f);
    Entity player = createAttachedPlayer();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    Body anchor = createAnchorBody();

    grapple.attachTo(anchor, new Vector2(5f, 6f));
    grapple.update();
    player.getEvents().trigger("grappleClimbStart");
    grapple.update();
    float firstRequestedLength = grapple.getRopeLength();

    // Do not step physics: the player cannot follow the shortened constraint.
    grapple.update();
    grapple.update();

    assertEquals(firstRequestedLength, grapple.getRopeLength(), 0.001f);
  }

  @Test
  void shouldNotClimbPastMinimumPlayerSegmentLength() {
    when(gameTime.getDeltaTime()).thenReturn(100f);
    Entity player = createAttachedPlayer();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    Body anchor = createAnchorBody();

    grapple.attachTo(anchor, new Vector2(5f, 6f));
    grapple.update();
    player.getEvents().trigger("grappleClimbStart");
    grapple.update();

    assertEquals(1f, grapple.getRopeLength(), 0.001f);
  }

  @Test
  void shouldTrackInitialLengthAndNotQueueBlockedDescent() {
    when(gameTime.getDeltaTime()).thenReturn(0.5f);
    Entity player = createAttachedPlayer();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
    Body anchor = createAnchorBody();

    grapple.attachTo(anchor, new Vector2(5f, 6f));
    grapple.update();
    float initialLength = grapple.getRopeLength();
    assertEquals(initialLength, grapple.getInitialRopeLength(), 0.001f);

    Vector2 grapplePoint = grapple.getAnchorPoint();
    Vector2 closerPosition = physics.getBody().getWorldCenter().cpy().lerp(grapplePoint, 0.25f);
    physics.getBody().setTransform(closerPosition, physics.getBody().getAngle());
    float actualLength = grapplePoint.dst(physics.getBody().getWorldCenter());

    player.getEvents().trigger("grappleDescendStart");
    grapple.update();
    float firstRequestedLength = grapple.getRopeLength();
    assertEquals(Math.min(initialLength, actualLength + 1.5f), firstRequestedLength, 0.001f);

    // Do not step physics: the player is blocked from following the extended constraint.
    grapple.update();
    grapple.update();

    assertEquals(firstRequestedLength, grapple.getRopeLength(), 0.001f);

    when(gameTime.getDeltaTime()).thenReturn(100f);
    grapple.update();
    assertEquals(initialLength, grapple.getRopeLength(), 0.001f);
  }

  @Test
  void shouldClearUnappliedLengthWhenControlIsReleased() {
    when(gameTime.getDeltaTime()).thenReturn(0.5f);
    Entity player = createAttachedPlayer();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    Body anchor = createAnchorBody();

    grapple.attachTo(anchor, new Vector2(5f, 6f));
    grapple.update();
    float actualLength = grapple.getRopeLength();

    player.getEvents().trigger("grappleClimbStart");
    grapple.update();
    assertTrue(grapple.getRopeLength() < actualLength);

    player.getEvents().trigger("grappleClimbStop");
    assertEquals(actualLength, grapple.getRopeLength(), 0.001f);
  }

  @Test
  void shouldNotChangeRopeLengthWhenClimbAndDescendAreBothHeld() {
    when(gameTime.getDeltaTime()).thenReturn(0.5f);
    Entity player = createAttachedPlayer();
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    Body anchor = createAnchorBody();

    grapple.attachTo(anchor, new Vector2(5f, 6f));
    grapple.update();
    float initialLength = grapple.getRopeLength();

    player.getEvents().trigger("grappleClimbStart");
    player.getEvents().trigger("grappleDescendStart");
    grapple.update();

    assertEquals(initialLength, grapple.getRopeLength(), 0.001f);
  }
}
