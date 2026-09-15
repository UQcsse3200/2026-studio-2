package com.csse3200.game.components.item.weapons.bow.grapple;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
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
  private static final float CLIMB_SPEED = 3f;
  private static final float MIN_PLAYER_SEGMENT_LENGTH = 1f;

  /** Keeps the rendered rope just outside the collider instead of clipping through its corner. */
  private static final float ROPE_RADIUS = 0.025f;

  private static final float RAY_END_TOLERANCE = 0.05f;
  private static final float SIDE_EPSILON = 0.0001f;
  private static final float MIN_JOINT_LENGTH = 0.05f;
  private static final int MAX_ROPE_CONTACTS = 16;

  private PhysicsComponent physicsComponent;
  private DistanceJoint ropeJoint;
  private Body originalAnchorBody;
  private Vector2 originalAnchorLocal;
  private float totalRopeLength;
  private float initialRopeLength;

  /** Bend points ordered from the original arrow anchor down towards the player. */
  private final List<RopeContact> ropeContacts = new ArrayList<>();

  private float cooldownRemaining = 0f;
  private boolean climbing;
  private boolean descending;
  // Whatever linearDamping the body had right before swinging, restored on release so a grapple
  // cycle never permanently changes the player's drag (and therefore jump height/speed).
  private float preSwingLinearDamping;

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
    entity.getEvents().addListener("grappleClimbStart", this::startClimbing);
    entity.getEvents().addListener("grappleClimbStop", this::stopClimbing);
    entity.getEvents().addListener("grappleDescendStart", this::startDescending);
    entity.getEvents().addListener("grappleDescendStop", this::stopDescending);
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
    updateRopeLength();
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
    totalRopeLength = 0f;
    initialRopeLength = 0f;
    ropeContacts.clear();
    physicsComponent.getBody().setLinearDamping(preSwingLinearDamping);
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
    // Capture the player's normal drag once for the whole grapple cycle. Bend changes rebuild the
    // joint and must not overwrite this with SWING_DAMPING.
    preSwingLinearDamping = physicsComponent.getBody().getLinearDamping();
    originalAnchorBody = anchorBody;
    originalAnchorLocal = anchorBody.getLocalPoint(point).cpy();
    totalRopeLength = physicsComponent.getBody().getWorldCenter().dst(point);
    initialRopeLength = totalRopeLength;
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

    // Stop the player spinning and bleed the swing off over time.
    playerBody.setFixedRotation(true);
    playerBody.setLinearDamping(SWING_DAMPING);
  }

  /** Maintains persistent bends while walking from the arrow anchor down towards the player. */
  private void updateRopeContact() {
    Vector2 player = physicsComponent.getBody().getWorldCenter();
    Vector2 anchor = getOriginalAnchorPoint();
    if (anchor == null) {
      return;
    }

    World world = ServiceLocator.getPhysicsService().getPhysics().getWorld();
    RopeContact previousActive = activeContact();
    removeUnwrappedContacts(world, anchor, player);
    insertMissingContacts(world, anchor, player);

    if (!sameContact(previousActive, activeContact())) {
      rebuildJointForPath();
    } else if (!ropeContacts.isEmpty()) {
      // Moving contacts change the length consumed above the active pivot every frame.
      ropeJoint.setLength(Math.max(remainingRopeLength(anchor), MIN_JOINT_LENGTH));
    }
  }

  private RopeContact activeContact() {
    return ropeContacts.isEmpty() ? null : ropeContacts.get(ropeContacts.size() - 1);
  }

  private boolean sameContact(RopeContact first, RopeContact second) {
    if (first == null || second == null) {
      return first == second;
    }
    return first.fixture == second.fixture && first.localPoint.epsilonEquals(second.localPoint);
  }

  private void removeUnwrappedContacts(World world, Vector2 anchor, Vector2 player) {
    boolean removed;
    do {
      removed = false;
      for (int i = ropeContacts.size() - 1; i >= 0; i--) {
        RopeContact contact = ropeContacts.get(i);
        Vector2 before = i == 0 ? anchor : ropeContacts.get(i - 1).getWorldPoint();
        Vector2 after =
            i == ropeContacts.size() - 1 ? player : ropeContacts.get(i + 1).getWorldPoint();
        if (contact.hasCrossedSide(before, after)
            && findObstruction(world, before, after) == null) {
          ropeContacts.remove(i);
          removed = true;
          break;
        }
      }
    } while (removed);
  }

  /** Inserts newly required contacts in anchor-to-player order without replacing existing bends. */
  private void insertMissingContacts(World world, Vector2 anchor, Vector2 player) {
    Vector2 before = anchor;
    int nextIndex = 0;
    int attempts = 0;
    while (nextIndex <= ropeContacts.size() && attempts++ < MAX_ROPE_CONTACTS) {
      if (ropeContacts.size() >= MAX_ROPE_CONTACTS) {
        break;
      }
      Vector2 after =
          nextIndex == ropeContacts.size() ? player : ropeContacts.get(nextIndex).getWorldPoint();
      RopeRayHit obstruction = findObstruction(world, before, after);
      if (obstruction == null) {
        before = after;
        nextIndex++;
        continue;
      }

      Vector2 point = findNextContact(world, obstruction, before, after, ropeContacts);
      if (point == null) {
        break;
      }
      RopeContact contact =
          new RopeContact(obstruction.fixture, point, sideOf(before, after, point));
      ropeContacts.add(nextIndex, contact);
      if (fixedPathLength(anchor, ropeContacts) > totalRopeLength - MIN_JOINT_LENGTH) {
        // This bend would consume more rope than exists and leave no valid player constraint.
        ropeContacts.remove(nextIndex);
        break;
      }
      before = point;
      nextIndex++;
    }
  }

  private void rebuildJointForPath() {
    if (ropeJoint != null) {
      ServiceLocator.getPhysicsService().getPhysics().getWorld().destroyJoint(ropeJoint);
      ropeJoint = null;
    }

    Vector2 anchor = getOriginalAnchorPoint();
    if (ropeContacts.isEmpty()) {
      createJointAt(originalAnchorBody, anchor, totalRopeLength);
      return;
    }

    RopeContact activeContact = activeContact();
    createJointAt(activeContact.body, activeContact.getWorldPoint(), remainingRopeLength(anchor));
  }

  private float remainingRopeLength(Vector2 anchor) {
    return totalRopeLength - fixedPathLength(anchor, ropeContacts);
  }

  /** Length consumed from the original anchor through every bend to the active player pivot. */
  static float fixedPathLength(Vector2 anchor, List<RopeContact> contacts) {
    float length = 0f;
    Vector2 previous = anchor;
    for (RopeContact contact : contacts) {
      Vector2 point = contact.getWorldPoint();
      length += previous.dst(point);
      previous = point;
    }
    return length;
  }

  private static RopeRayHit findObstruction(World world, Vector2 from, Vector2 to) {
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
    world.rayCast(callback, from, to);
    return closest.fixture == null ? null : closest;
  }

  /**
   * Repeatedly finds the first obstruction below the current rope point. The next bend is the
   * shortest visible vertex of that obstruction; the search then resumes from that bend.
   */
  static List<RopeContact> traceContacts(World world, Vector2 anchor, Vector2 player) {
    List<RopeContact> contacts = new ArrayList<>();
    Vector2 cursor = anchor.cpy();
    for (int i = 0; i < MAX_ROPE_CONTACTS; i++) {
      RopeRayHit obstruction = findObstruction(world, cursor, player);
      if (obstruction == null) {
        break;
      }
      Vector2 next = findNextContact(world, obstruction, cursor, player, contacts);
      if (next == null) {
        break;
      }
      RopeContact contact =
          new RopeContact(obstruction.fixture, next, sideOf(cursor, player, next));
      contacts.add(contact);
      cursor = next;
    }
    return contacts;
  }

  /**
   * Chooses the shortest visible vertex from the current rope point. Visibility, rather than an
   * arbitrary vertex rank, makes the front edge appear first and lets the same search discover each
   * following edge as it moves down the rope.
   */
  private static Vector2 findNextContact(
      World world,
      RopeRayHit hit,
      Vector2 from,
      Vector2 target,
      List<RopeContact> existingContacts) {
    Shape shape = hit.fixture.getShape();
    if (!(shape instanceof PolygonShape polygon)) {
      return null;
    }

    Body body = hit.fixture.getBody();
    Vector2 local = new Vector2();
    Vector2 centre = new Vector2();
    for (int i = 0; i < polygon.getVertexCount(); i++) {
      polygon.getVertex(i, local);
      centre.add(body.getWorldPoint(local));
    }
    centre.scl(1f / polygon.getVertexCount());

    Vector2 best = null;
    float bestRoute = Float.MAX_VALUE;
    for (int i = 0; i < polygon.getVertexCount(); i++) {
      polygon.getVertex(i, local);
      Vector2 vertex = offsetFromCollider(body.getWorldPoint(local).cpy(), centre);
      if (alreadyUsed(hit.fixture, vertex, existingContacts)
          || findObstruction(world, from, vertex) != null) {
        continue;
      }
      float route = from.dst(vertex) + vertex.dst(target);
      if (route < bestRoute) {
        bestRoute = route;
        best = vertex;
      }
    }
    return best;
  }

  private static boolean alreadyUsed(
      Fixture fixture, Vector2 point, List<RopeContact> existingContacts) {
    for (RopeContact contact : existingContacts) {
      if (contact.fixture == fixture
          && contact.getWorldPoint().epsilonEquals(point, RAY_END_TOLERANCE)) {
        return true;
      }
    }
    return false;
  }

  static int sideOf(Vector2 from, Vector2 to, Vector2 point) {
    float cross = to.cpy().sub(from).crs(point.cpy().sub(from));
    if (Math.abs(cross) <= SIDE_EPSILON) {
      return 0;
    }
    return cross > 0f ? 1 : -1;
  }

  private static Vector2 offsetFromCollider(Vector2 vertex, Vector2 centre) {
    Vector2 outward = vertex.cpy().sub(centre);
    if (!outward.isZero()) {
      vertex.mulAdd(outward.nor(), ROPE_RADIUS);
    }
    return vertex;
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

  /** Starts retracting the rope while the climb control is held. */
  public void startClimbing() {
    climbing = true;
  }

  /** Stops retracting the rope and clears any unapplied constraint adjustment. */
  public void stopClimbing() {
    climbing = false;
    syncRopeLengthToActualPath();
  }

  /** Starts extending the rope while the descend control is held. */
  public void startDescending() {
    descending = true;
  }

  /** Stops extending the rope and clears any unapplied constraint adjustment. */
  public void stopDescending() {
    descending = false;
    syncRopeLengthToActualPath();
  }

  private void updateRopeLength() {
    if (!isAttached() || climbing == descending) {
      return;
    }

    Vector2 anchor = getOriginalAnchorPoint();
    if (anchor == null) {
      return;
    }

    float fixedLength = fixedPathLength(anchor, ropeContacts);
    float actualTotalLength = fixedLength + ropeJoint.getAnchorA().dst(ropeJoint.getAnchorB());
    float adjustment = CLIMB_SPEED * ServiceLocator.getTimeSource().getDeltaTime();
    setTotalRopeLength(actualTotalLength + (descending ? adjustment : -adjustment), fixedLength);
  }

  private void syncRopeLengthToActualPath() {
    if (!isAttached()) {
      return;
    }

    Vector2 anchor = getOriginalAnchorPoint();
    if (anchor == null) {
      return;
    }

    float fixedLength = fixedPathLength(anchor, ropeContacts);
    float actualTotalLength = fixedLength + ropeJoint.getAnchorA().dst(ropeJoint.getAnchorB());
    setTotalRopeLength(actualTotalLength, fixedLength);
  }

  private void setTotalRopeLength(float requestedLength, float fixedLength) {
    float minimumLength = Math.min(initialRopeLength, fixedLength + MIN_PLAYER_SEGMENT_LENGTH);
    totalRopeLength = Math.max(minimumLength, Math.min(initialRopeLength, requestedLength));
    ropeJoint.setLength(Math.max(totalRopeLength - fixedLength, MIN_JOINT_LENGTH));
  }

  public boolean isAttached() {
    return ropeJoint != null;
  }

  /**
   * @return the current total rope length, including bends, or 0 when detached
   */
  public float getRopeLength() {
    return ropeJoint == null ? 0f : totalRopeLength;
  }

  /**
   * @return the rope length when it first attached, or 0 when detached
   */
  public float getInitialRopeLength() {
    return initialRopeLength;
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
    for (int i = ropeContacts.size() - 1; i >= 0; i--) {
      points.add(ropeContacts.get(i).getWorldPoint());
    }
    Vector2 anchor = getOriginalAnchorPoint();
    if (anchor != null) {
      points.add(anchor);
    }
    return points;
  }

  static class RopeContact {
    private final Fixture fixture;
    private final Body body;
    private final Vector2 localPoint;
    private final int initialSide;

    RopeContact(Fixture fixture, Vector2 worldPoint, int initialSide) {
      this.fixture = fixture;
      this.body = fixture.getBody();
      this.localPoint = body.getLocalPoint(worldPoint).cpy();
      this.initialSide = initialSide;
    }

    Vector2 getWorldPoint() {
      return body.getWorldPoint(localPoint).cpy();
    }

    Fixture getFixture() {
      return fixture;
    }

    int getInitialSide() {
      return initialSide;
    }

    boolean hasCrossedSide(Vector2 before, Vector2 after) {
      int currentSide = sideOf(before, after, getWorldPoint());
      return currentSide != 0 && initialSide != 0 && currentSide != initialSide;
    }
  }

  private static class RopeRayHit {
    private Fixture fixture;
  }
}
