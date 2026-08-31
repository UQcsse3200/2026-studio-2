package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

/** Fires a grapple arrow, then swings from wherever it lands. */
public class GrappleComponent extends Component {
  private static final float GRAPPLE_COOLDOWN = 2f;
  private static final float SWING_FORCE = 5f;
  private static final float MAX_SWING_SPEED = 7f;
  private static final float SWING_DAMPING = 0.5f;
  private static final float RELEASE_DAMPING = 0f;

  private PhysicsComponent physicsComponent;
  private DistanceJoint ropeJoint;
  private Vector2 anchorPoint;
  private float cooldownRemaining = 0f;

  // Box2D locks the world during a step, so attachments are queued and built next frame
  private Body pendingAnchorBody;
  private Vector2 pendingAnchorPoint;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    entity.getEvents().addListener("grappleFire", this::fire);
    entity.getEvents().addListener("grappleRelease", this::release);
    entity.getEvents().addListener("grappleSwing", this::swing);
  }

  @Override
  public void update() {
    if (cooldownRemaining > 0f) {
      cooldownRemaining -= ServiceLocator.getTimeSource().getDeltaTime();
    }
    if (pendingAnchorBody != null) {
      createJoint(pendingAnchorBody, pendingAnchorPoint);
      pendingAnchorBody = null;
      pendingAnchorPoint = null;
    }
  }

  /** Launches a grapple arrow, unless one is in flight, attached, or still on cooldown. */
  void fire(Vector2 direction) {
    if (direction == null || direction.isZero() || cooldownRemaining > 0f || isAttached()) {
      return;
    }

    Vector2 aim = direction.cpy().nor();
    Vector2 spawn = entity.getCenterPosition().mulAdd(aim, entity.getScale().x * 0.6f);

    Entity arrow = ProjectileFactory.createGrappleArrow(spawn, aim);
    arrow.addComponent(new GrappleArrowComponent(entity));
    ServiceLocator.getEntityService().register(arrow);

    // Only set on a successful shot, so spamming the button doesn't extend the wait
    cooldownRemaining = GRAPPLE_COOLDOWN;
  }

  /**
   * Queues a rope attachment. Called by the arrow when it lands, which happens mid physics step, so
   * the joint itself is built on the next update.
   *
   * @param anchorBody the body that was hit
   * @param point where the arrow struck, in world coordinates
   */
  public void attachTo(Body anchorBody, Vector2 point) {
    if (isAttached() || pendingAnchorBody != null) {
      return;
    }
    pendingAnchorBody = anchorBody;
    pendingAnchorPoint = point.cpy();
  }

  private void createJoint(Body anchorBody, Vector2 point) {
    anchorPoint = point.cpy();
    Body playerBody = physicsComponent.getBody();

    DistanceJointDef def = new DistanceJointDef();
    def.bodyA = anchorBody;
    def.bodyB = playerBody;

    // Convert world anchor coordinates to the anchor body's local space
    def.localAnchorA.set(anchorBody.getLocalPoint(anchorPoint));

    // Pivot from the player's centre of mass so the pendulum hangs evenly
    def.localAnchorB.set(playerBody.getLocalCenter());

    // Fixed length keeps the player on the arc so momentum carries to the other side
    def.length = playerBody.getPosition().dst(anchorPoint);
    def.frequencyHz = 0f; // 0 = rigid rod, raise for a springier rope
    def.dampingRatio = 0f;
    def.collideConnected = true;

    ropeJoint =
        (DistanceJoint) ServiceLocator.getPhysicsService().getPhysics().getWorld().createJoint(def);

    // Stop the player spinning, and bleed the swing off over time
    playerBody.setFixedRotation(true);
    playerBody.setLinearDamping(SWING_DAMPING);
  }

  /** Detaches the rope, restoring free movement. Momentum carries over. */
  public void release() {
    if (!isAttached()) {
      return;
    }
    ServiceLocator.getPhysicsService().getPhysics().getWorld().destroyJoint(ropeJoint);
    ropeJoint = null;
    anchorPoint = null;
    physicsComponent.getBody().setLinearDamping(RELEASE_DAMPING);
  }

  /**
   * Pushes along the swing arc while anchored, so holding a direction builds speed.
   *
   * @param direction -1 for left, +1 for right, 0 for no input
   */
  void swing(float direction) {
    // No input means let the pendulum swing freely, otherwise it drifts to one side
    if (!isAttached() || direction == 0f) {
      return;
    }

    Body body = physicsComponent.getBody();

    // Cap the speed so you can't pump forever
    if (body.getLinearVelocity().len() > MAX_SWING_SPEED) {
      return;
    }

    // Vector from the anchor out to the player
    Vector2 r = entity.getCenterPosition().sub(anchorPoint);

    // Rotate 90 degrees one way or the other depending on which key is held
    Vector2 tangent = direction > 0 ? new Vector2(-r.y, r.x).nor() : new Vector2(r.y, -r.x).nor();

    body.applyForceToCenter(tangent.scl(SWING_FORCE * body.getMass()), true);
  }

  public boolean isAttached() {
    return ropeJoint != null;
  }

  public Vector2 getAnchorPoint() {
    return anchorPoint == null ? null : anchorPoint.cpy();
  }
}
