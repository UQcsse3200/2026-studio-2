package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
    grapple.swing(1f);
    grapple.swing(-1f);
    grapple.swing(0f);
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
    grapple.fire(null);
    grapple.fire(Vector2.Zero.cpy());
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
