package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

/** Player fires a rope */
public class GrappleComponent extends Component {
  private static final float MAX_RANGE = 8f;
  private static final float BASE_SWING_FORCE = 30f;
  private static final float SWING_SCALE = 20f;
  private static final short TARGETS = (short) (PhysicsLayer.OBSTACLE | PhysicsLayer.GROUND);

  private PhysicsComponent physicsComponent;
  private RopeJoint ropeJoint;
  private Vector2 anchorPoint;
  private Vector2 shotFrom;
  private float velocityX;
  private float velocityY;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    entity.getEvents().addListener("grappleFire", this::fire);
    entity.getEvents().addListener("grappleSwing", this::swing);
  }

  /** Fires the rope towards direction or detaches if already attached. */
  void fire(Vector2 direction) {
    if (isAttached()) {
      release();
      return;
    }

    // Determine raycast start (player center)
    Vector2 start = entity.getCenterPosition();
    Vector2 end = start.cpy().mulAdd(direction.cpy().nor(), MAX_RANGE);
    RaycastHit hit = new RaycastHit();

    // Perform Box2D raycast against terrain/obstacle layers and exits if no surface was hit
    if (!ServiceLocator.getPhysicsService().getPhysics().raycast(start, end, TARGETS, hit)) {
      return;
    }

    // Store fired position and retrieve physics bodies for joint setup
    anchorPoint = hit.point.cpy();
    Body playerBody = physicsComponent.getBody();
    Body anchorBody = hit.fixture.getBody();

    RopeJointDef def = new RopeJointDef();
    def.bodyA = anchorBody;
    def.bodyB = playerBody;

    // Convert world anchor coordinates to the anchor body's local space
    def.localAnchorA.set(anchorBody.getLocalPoint(anchorPoint));
    def.localAnchorB.set(playerBody.getLocalPoint(start));

    // Set max rope length to the current straight-line distance to the wall
    def.maxLength = start.dst(anchorPoint);
    def.collideConnected = true;
    ropeJoint =
        (RopeJoint) ServiceLocator.getPhysicsService().getPhysics().getWorld().createJoint(def);
  }

  /** Detaches the rope, restoring free movement. */
  void release() {
    if (!isAttached()) return;

    ServiceLocator.getPhysicsService().getPhysics().getWorld().destroyJoint(ropeJoint);
    ropeJoint = null;
    anchorPoint = null;
  }

  /** Applies horizontal swing force while anchored. */
  void swing(float direction) {
    if (!isAttached()) return;

    float offsetX = entity.getCenterPosition().x - anchorPoint.x;
    float ropeLength = ropeJoint.getMaxLength();

    float swingRatio = Math.min(1f, Math.abs(offsetX) / ropeLength);
    float force = BASE_SWING_FORCE + (SWING_SCALE * swingRatio);

    Body body = physicsComponent.getBody();
    body.applyForceToCenter(direction * force * body.getMass(), 0, true);
  }

  public boolean isAttached() {
    return ropeJoint != null;
  }

  public Vector2 getAnchorPoint() {
    return anchorPoint == null ? null : anchorPoint.cpy();
  }
}
