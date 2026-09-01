package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

/** Player fires a rope and swings from it like a pendulum. */
public class GrappleComponent extends Component {
  private static final float MAX_RANGE = 8f;
  private static final float SWING_FORCE = 5f;
  private static final float MAX_SWING_SPEED = 7f;
  private static final float SWING_DAMPING = 0.5f;

  private PhysicsComponent physicsComponent;
  private DistanceJoint ropeJoint;
  private RaycastHit raycastHit;
  private Vector2 anchorPoint;
  private Body anchorBody;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    entity.getEvents().addListener("grappleFire", this::fire);
    entity.getEvents().addListener("grappleRelease", this::release);
    entity.getEvents().addListener("grappleSwing", this::swing);
    entity.getEvents().addListener("grappleResponse", this::handleSuccessfulFire);
  }

  /** Fires the rope towards direction or detaches if already attached. */
  void fire(Vector2 direction) {
    // Determine raycast start (player center)
    Vector2 start = entity.getCenterPosition();
    Vector2 end = start.cpy().mulAdd(direction.cpy().nor(), MAX_RANGE);
    raycastHit = new RaycastHit();

    // Perform Box2D raycast against terrain/obstacle layers and exits if no surface was hit
    if (!ServiceLocator.getPhysicsService()
        .getPhysics()
        .raycast(start, end, PhysicsLayer.SOLID, raycastHit)) {
      return;
    }

    // send event to request whether the grapple raycast hits a valid platform side
    entity.getEvents().trigger("grappleRequested", raycastHit.point.cpy());
  }

  void handleSuccessfulFire(boolean success) {
    if (!success) return;

    // Store fired position and retrieve physics bodies for joint setup
    Vector2 start = entity.getCenterPosition();
    anchorPoint = raycastHit.point.cpy();
    Body playerBody = physicsComponent.getBody();
    this.anchorBody = raycastHit.fixture.getBody();

    DistanceJointDef def = new DistanceJointDef();
    def.bodyA = anchorBody;
    def.bodyB = playerBody;

    // Convert world anchor coordinates to the anchor body's local space
    def.localAnchorA.set(anchorBody.getLocalPoint(anchorPoint));

    // Pivot from the player's centre of mass so the pendulum hangs evenly
    def.localAnchorB.set(playerBody.getLocalCenter());

    // Fixed length keeps the player on the arc so momentum carries to the other side
    def.length = start.dst(anchorPoint);
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
  void release() {
    if (!isAttached()) return;

    ServiceLocator.getPhysicsService().getPhysics().getWorld().destroyJoint(ropeJoint);
    ropeJoint = null;
    anchorPoint = null;

    physicsComponent.getBody().setLinearDamping(2f);
  }

  /**
   * Pushes along the swing arc while anchored, so holding a direction builds speed.
   *
   * @param direction -1 for left, +1 for right, 0 for no input
   */
  void swing(float direction) {
    // No input means let the pendulum swing freely, otherwise it drifts to one side
    if (!isAttached() || direction == 0f) return;

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
    // return anchorPoint == null ? null : anchorPoint.cpy();
    if (!isAttached() || anchorBody == null) {
      return null;
    }
    return anchorBody.getWorldPoint(ropeJoint.getLocalAnchorA());
  }
}
