package com.csse3200.game.components.item.weapons.bow.grapple;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;

/** Fires a grapple arrow, then swings from wherever it lands. */
public class GrappleComponent extends Component {

  private static final float GRAPPLE_COOLDOWN = 2f;
  private static final float SWING_FORCE = 7f;
  private static final float MAX_SWING_SPEED = 7f;
  private static final float SWING_DAMPING = 0.5f;
  private static final float RELEASE_DAMPING = 0f;

  /** Keeps the rendered rope just outside the collider instead of clipping through its corner. */
  private static final float ROPE_RADIUS = 0.025f;

  private static final float RAY_END_TOLERANCE = 0.05f;
  private static final float MIN_JOINT_LENGTH = 0.05f;

  private PhysicsComponent physicsComponent;
  private DistanceJoint ropeJoint;
  private Body originalAnchorBody;
  private Vector2 originalAnchorLocal;
  private float totalRopeLength;
  private RopeContact ropeContact;
  private float cooldownRemaining = 0f;

  // Box2D locks the world during a step, so attachments are queued and built next frame
  private Body pendingAnchorBody;
  private Vector2 pendingAnchorPoint;

  // Reused when checking the joint is still alive, to avoid allocating every frame
  private final Array<Joint> liveJoints = new Array<>();

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
    // Box2D silently destroys the joint if its anchor body is removed (e.g. a grappled enemy dies)
    if (ropeJoint != null && !jointIsAlive()) {
      forgetJoint();
    }
    if (ropeJoint != null) {
      updateRopeContact();
    }
    if (pendingAnchorBody != null) {
      createJoint(pendingAnchorBody, pendingAnchorPoint);
      pendingAnchorBody = null;
      pendingAnchorPoint = null;
    }
  }

  private boolean jointIsAlive() {
    liveJoints.clear();
    ServiceLocator.getPhysicsService().getPhysics().getWorld().getJoints(liveJoints);
    return liveJoints.contains(ropeJoint, true);
  }

  /** Clears rope state after the joint has already gone, without touching the dead joint. */
  private void forgetJoint() {
    ropeJoint = null;
    originalAnchorBody = null;
    originalAnchorLocal = null;
    ropeContact = null;
    physicsComponent.getBody().setLinearDamping(RELEASE_DAMPING);
  }

  /** Launches a grapple arrow, unless one is in flight, attached, or still on cooldown. */
  public void fire(Vector2 direction) {
    if (direction == null || direction.isZero() || cooldownRemaining > 0f || isAttached()) {
      return;
    }

    Vector2 aim = direction.cpy().nor();
    Vector2 spawn = entity.getCenterPosition().mulAdd(aim, entity.getScale().x * 0.6f);

    // Pass 'entity' so the grapple arrow ignores player collisions
    Entity arrow = ProjectileFactory.createGrappleArrow(entity, spawn, aim);
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
    originalAnchorBody = anchorBody;
    originalAnchorLocal = anchorBody.getLocalPoint(point).cpy();
    totalRopeLength = physicsComponent.getBody().getWorldCenter().dst(point);
    createJointAt(anchorBody, point, totalRopeLength);
  }

  private void createJointAt(Body anchorBody, Vector2 point, float length) {
    Body playerBody = physicsComponent.getBody();

    DistanceJointDef def = new DistanceJointDef();
    def.bodyA = anchorBody;
    def.bodyB = playerBody;

    // Convert world anchor coordinates to the anchor body's local space
    def.localAnchorA.set(anchorBody.getLocalPoint(point));

    // Pivot from the player's centre of mass so the pendulum hangs evenly
    def.localAnchorB.set(playerBody.getLocalCenter());

    // Fixed length keeps the player on the arc so momentum carries to the other side
    def.length = Math.max(length, MIN_JOINT_LENGTH);
    def.frequencyHz = 0f; // 0 = rigid rod, raise for a springier rope
    def.dampingRatio = 0f;
    def.collideConnected = true;

    ropeJoint =
        (DistanceJoint) ServiceLocator.getPhysicsService().getPhysics().getWorld().createJoint(def);

    // Stop the player spinning, and bleed the swing off over time
    playerBody.setFixedRotation(true);
    playerBody.setLinearDamping(SWING_DAMPING);
  }

  /** Adds or removes the wall-top pivot as the player moves behind terrain. */
  private void updateRopeContact() {
    Vector2 player = physicsComponent.getBody().getWorldCenter();
    Vector2 anchor = getOriginalAnchorPoint();
    if (anchor == null) {
      return;
    }

    RopeRayHit obstruction = findObstruction(player, anchor);
    if (ropeContact == null) {
      if (obstruction != null) {
        Vector2 corner = findTopContact(obstruction.fixture, player, anchor);
        if (corner != null) {
          ropeContact = new RopeContact(obstruction.fixture.getBody(), corner);
          rebuildJointForPath();
        }
      }
    } else if (obstruction == null || obstruction.fixture.getBody() != ropeContact.body) {
      ropeContact = null;
      rebuildJointForPath();
    }
  }

  private void rebuildJointForPath() {
    if (ropeJoint != null) {
      ServiceLocator.getPhysicsService().getPhysics().getWorld().destroyJoint(ropeJoint);
      ropeJoint = null;
    }

    Vector2 anchor = getOriginalAnchorPoint();
    if (ropeContact == null) {
      createJointAt(originalAnchorBody, anchor, totalRopeLength);
      return;
    }

    Vector2 contact = ropeContact.getWorldPoint();
    float fixedLength = contact.dst(anchor);
    createJointAt(ropeContact.body, contact, totalRopeLength - fixedLength);
  }

  /** Finds the closest solid strictly between the player and grapple anchor. */
  private RopeRayHit findObstruction(Vector2 from, Vector2 to) {
    RopeRayHit closest = new RopeRayHit();
    RayCastCallback callback =
        (fixture, point, normal, fraction) -> {
          if (!PhysicsLayer.contains(PhysicsLayer.SOLID, fixture.getFilterData().categoryBits)) {
            return -1f;
          }
          // The grapple anchor's own fixture is expected at the end of every ray.
          if (point.dst2(to) <= RAY_END_TOLERANCE * RAY_END_TOLERANCE) {
            return -1f;
          }
          closest.fixture = fixture;
          return fraction;
        };
    ServiceLocator.getPhysicsService().getPhysics().getWorld().rayCast(callback, from, to);
    return closest.fixture == null ? null : closest;
  }

  /**
   * Chooses the shortest route via the upper silhouette of a polygon collider. World-space fixture
   * vertices make this account for the wall's real width, height, offset and rotation.
   */
  static Vector2 findTopContact(Fixture fixture, Vector2 player, Vector2 anchor) {
    Shape shape = fixture.getShape();
    if (!(shape instanceof PolygonShape polygon)) {
      return null;
    }

    Body body = fixture.getBody();
    Vector2 local = new Vector2();
    Vector2 best = null;
    float highest = -Float.MAX_VALUE;
    float shortestRoute = Float.MAX_VALUE;
    for (int i = 0; i < polygon.getVertexCount(); i++) {
      polygon.getVertex(i, local);
      Vector2 vertex = body.getWorldPoint(local);
      float route = player.dst(vertex) + vertex.dst(anchor);
      if (vertex.y > highest + ROPE_RADIUS
          || (Math.abs(vertex.y - highest) <= ROPE_RADIUS && route < shortestRoute)) {
        highest = vertex.y;
        shortestRoute = route;
        // Body reuses a temporary vector for coordinate transforms, so retain our own value.
        best = vertex.cpy();
      }
    }
    return best == null ? null : best.add(0f, ROPE_RADIUS);
  }

  /** Detaches the rope, restoring free movement. Momentum carries over. */
  public void release() {
    if (!isAttached()) {
      return;
    }

    ServiceLocator.getPhysicsService().getPhysics().getWorld().destroyJoint(ropeJoint);
    forgetJoint();
  }

  @Override
  public void dispose() {
    // The physics world disposes its own joints on teardown, and destroying the player body
    // takes this joint with it, so just drop the reference.
    ropeJoint = null;
  }

  /**
   * Pushes along the swing arc while anchored, so holding a direction builds speed.
   *
   * @param direction -1 for left, +1 for right, 0 for no input
   */
  public void swing(float direction) {
    // No input means let the pendulum swing freely, otherwise it drifts to one side
    if (!isAttached() || direction == 0f) {
      return;
    }

    Body body = physicsComponent.getBody();

    // Cap the speed so you can't pump forever
    if (body.getLinearVelocity().len() > MAX_SWING_SPEED) {
      return;
    }

    // Vector from the anchor out to the player (live, so it tracks a moving anchor)
    Vector2 r = entity.getCenterPosition().sub(ropeJoint.getAnchorA());

    // Rotate 90 degrees one way or the other depending on which key is held
    Vector2 tangent = direction > 0 ? new Vector2(-r.y, r.x).nor() : new Vector2(r.y, -r.x).nor();

    body.applyForceToCenter(tangent.scl(SWING_FORCE * body.getMass()), true);
  }

  public boolean isAttached() {
    return ropeJoint != null;
  }

  /**
   * @return the rope's current world anchor, tracking the anchor body if it moves, or null when not
   *     attached
   */
  public Vector2 getAnchorPoint() {
    return ropeJoint == null ? null : ropeJoint.getAnchorA().cpy();
  }

  private Vector2 getOriginalAnchorPoint() {
    return originalAnchorBody == null || originalAnchorLocal == null
        ? null
        : originalAnchorBody.getWorldPoint(originalAnchorLocal).cpy();
  }

  /**
   * @return ordered world-space points used to draw the taut rope, from player to arrow anchor.
   */
  public List<Vector2> getRopePath() {
    List<Vector2> points = new ArrayList<>(3);
    if (!isAttached()) {
      return points;
    }
    points.add(physicsComponent.getBody().getWorldCenter().cpy());
    if (ropeContact != null) {
      points.add(ropeContact.getWorldPoint());
    }
    Vector2 anchor = getOriginalAnchorPoint();
    if (anchor != null) {
      points.add(anchor);
    }
    return points;
  }

  private static class RopeContact {
    private final Body body;
    private final Vector2 localPoint;

    RopeContact(Body body, Vector2 worldPoint) {
      this.body = body;
      this.localPoint = body.getLocalPoint(worldPoint).cpy();
    }

    Vector2 getWorldPoint() {
      return body.getWorldPoint(localPoint).cpy();
    }
  }

  private static class RopeRayHit {
    private Fixture fixture;
  }
}
