package com.csse3200.game.components.item.weapons.bow.grapple;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.weapons.bow.BowCharge;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;

/** Fires a grapple arrow, then swings from wherever it lands. */
public class GrappleComponent extends Component {

  /** How far in front of the player's centre, in player widths, a fired arrow spawns. */
  public static final float SPAWN_OFFSET = 0.6f;

  private static final float GRAPPLE_COOLDOWN = 2f;
  private static final float SWING_FORCE = 7f;
  private static final float MAX_SWING_SPEED = 7f;
  private static final float SWING_DAMPING = 0.5f;
  private static final float CLIMB_SPEED = 3f;
  private static final float MIN_PLAYER_SEGMENT_LENGTH = 1f;
  private static final float CORNER_CLEARANCE = 0.1f;
  private static final float CORNER_APPROACH_DISTANCE = 0.25f;

  // Below this the swing arc is nearly vertical (player level with the anchor), so left/right can't
  // pick a side from where the push points and it falls back to a fixed convention instead.
  private static final float MIN_TANGENT_X = 0.05f;

  /** Keeps the rendered rope just outside the collider instead of clipping through its corner. */
  private static final float ROPE_RADIUS = 0.025f;

  private static final float RAY_END_TOLERANCE = 0.05f;
  private static final float SIDE_EPSILON = 0.0001f;
  private static final float MIN_JOINT_LENGTH = 0.05f;
  private static final int MAX_ROPE_CONTACTS = 16;

  private PhysicsComponent physicsComponent;
  private RopeJoint ropeJoint;
  private Body originalAnchorBody;
  private Vector2 originalAnchorLocal;
  private float totalRopeLength;
  private float initialRopeLength;

  /** Bend points ordered from the original arrow anchor down towards the player. */
  private final List<RopeContact> ropeContacts = new ArrayList<>();

  private float cooldownRemaining = 0f;
  private boolean climbing;
  private CornerClimb cornerClimb;
  private boolean descending;
  private boolean charging;
  private long chargeStartTimeMs;
  // Whatever linearDamping the body had right before swinging, restored on release so a grapple
  // cycle never permanently changes the player's drag (and therefore jump height/speed).
  private float preSwingLinearDamping;

  // Box2D locks the world during a step, so attachments are queued and built next frame
  private Body pendingAnchorBody;
  private Vector2 pendingAnchorPoint;

  // The arrow currently in flight, if any - tracked so a respawn can cancel it before it can land
  // and attach a rope back to wherever the player died.
  private Entity pendingArrow;

  // Reused when checking the joint is still alive, to avoid allocating every frame
  private final Array<Joint> liveJoints = new Array<>();
  private final Array<Body> liveBodies = new Array<>();

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    entity.getEvents().addListener("grappleFire", (Vector2 direction) -> fire(direction));
    entity.getEvents().addListener("grappleRelease", this::release);
    entity.getEvents().addListener("grappleSwing", this::swing);
    entity.getEvents().addListener("grappleClimbStart", this::startClimbing);
    entity.getEvents().addListener("grappleClimbStop", this::stopClimbing);
    entity.getEvents().addListener("grappleDescendStart", this::startDescending);
    entity.getEvents().addListener("grappleDescendStop", this::stopDescending);
    entity.getEvents().addListener("grappleDrawStart", this::startCharge);
    entity.getEvents().addListener("grappleDrawRelease", this::releaseCharge);
    entity.getEvents().addListener("chargeCancel", this::cancelCharge);
    entity.getEvents().addListener("death", this::cancelCharge);
    entity.getEvents().addListener("respawnAtCheckpoint", this::resetOnRespawn);
  }

  /**
   * Begins charging a grapple shot on shoot-button-down, mirroring the bow's own hold-to-draw.
   * Deliberately its own event, separate from the bow's "chargeStart"/"chargeRelease", so the two
   * weapons never cross-trigger each other off the same shared broadcast. No-ops (and fires no
   * animation event) if already attached, already charging, or still on cooldown from the last
   * shot.
   *
   * @param direction Aim direction at the moment charging started.
   */
  public void startCharge(Vector2 direction) {
    if (direction == null || direction.isZero() || isAttached() || isOnCooldown() || charging) {
      return;
    }
    charging = true;
    chargeStartTimeMs = ServiceLocator.getTimeSource().getTime();
    // Only fires once the charge is actually accepted, so no draw animation plays for a press that
    // did nothing - e.g. while the grapple is still on cooldown.
    entity.getEvents().trigger("grappleChargeStart", direction);
  }

  /**
   * Fires the currently charging shot, if any, on shoot-button-release, with launch speed scaled by
   * how long it was held exactly as the bow's is (see {@link BowCharge}) - a tap barely lobs it, a
   * full draw flings it far. No-ops if nothing was charging.
   *
   * @param direction Aim direction at release time.
   */
  public void releaseCharge(Vector2 direction) {
    if (!charging) {
      return;
    }
    float speedMultiplier = currentSpeedMultiplier();
    charging = false;
    entity.getEvents().trigger("grappleChargeFire", direction);
    fire(direction, speedMultiplier);
  }

  /**
   * Cancels an in-progress charge without firing, e.g. if the player dies mid-draw or a UI overlay
   * steals the mouse-up.
   */
  private void cancelCharge() {
    charging = false;
  }

  /**
   * @return the speed multiplier a release right now would fire with, or 1 when not charging - used
   *     by the aim preview to show exactly where the current charge would land, and by {@link
   *     #releaseCharge} to scale the actual shot
   */
  public float currentSpeedMultiplier() {
    if (!charging) {
      return 1f;
    }
    long now = ServiceLocator.getTimeSource().getTime();
    return BowCharge.speedMultiplier(now - chargeStartTimeMs);
  }

  /**
   * @return true while a shot is being charged, i.e. between the shoot button going down and coming
   *     back up
   */
  public boolean isCharging() {
    return charging;
  }

  /**
   * Clears every bit of grapple state on respawn - otherwise an arrow fired right before falling
   * can still land and attach a rope back to wherever you died, well after you've been teleported
   * to the checkpoint.
   */
  private void resetOnRespawn() {
    release();
    cancelCharge();
    cooldownRemaining = 0f;
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
    if (cornerClimb != null) {
      // The rope may have unwrapped onto a different body while this climb is still finishing.
      ServiceLocator.getPhysicsService().getPhysics().getWorld().getBodies(liveBodies);
      // Body and fixture wrappers are pooled. Their cached shape identity changes on reuse.
      if (!liveBodies.contains(cornerClimb.body(), true)
          || !cornerClimb.body().getFixtureList().contains(cornerClimb.fixture(), true)
          || cornerClimb.fixture().getShape() != cornerClimb.shape()) {
        cornerClimb = null;
      }
    }
    if (ropeJoint != null) {
      updateRopeContact();
    }
    if (pendingAnchorBody != null) {
      createJoint(pendingAnchorBody, pendingAnchorPoint);
      pendingAnchorBody = null;
      pendingAnchorPoint = null;
    }
    if (pendingArrow != null) {
      updateFlightRope(arrowPoint());
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
    cornerClimb = null;
    physicsComponent.getBody().setLinearDamping(preSwingLinearDamping);
  }

  /** Launches a normal-speed grapple arrow unless attached or on cooldown. */
  public void fire(Vector2 direction) {
    fire(direction, 1f);
  }

  /**
   * Launches a grapple arrow unless attached or still on cooldown. Replaces a previous missed shot.
   *
   * @param speedMultiplier scales the arrow's launch speed - 1 for a normal shot, lower for one
   *     released early out of a charge, higher for a fully drawn one
   */
  public void fire(Vector2 direction, float speedMultiplier) {
    if (direction == null
        || direction.isZero()
        || cooldownRemaining > 0f
        || isAttached()
        || pendingAnchorBody != null) {
      return;
    }
    try {
      Sound arrowSound =
          ServiceLocator.getResourceService().getAsset("sounds/Arrow_release.wav", Sound.class);
      arrowSound.play(0.4f);
    } catch (Exception e) {
      // skip
    }

    Vector2 aim = direction.cpy().nor();
    Vector2 spawn = entity.getCenterPosition().mulAdd(aim, entity.getScale().x * SPAWN_OFFSET);

    // Pass 'entity' so the grapple arrow ignores player collisions
    Entity arrow = ProjectileFactory.createGrappleArrow(entity, spawn, aim, speedMultiplier);
    arrow.addComponent(new GrappleArrowComponent(entity));
    // A previous missed shot may still be flying after its cooldown. Retire it before replacing
    // the rope endpoint, so its later collision or disposal cannot affect this shot.
    cancelFlight();
    ServiceLocator.getEntityService().register(arrow);
    pendingArrow = arrow;

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
    // Capture the final segment before the arrow is removed. Raycasts are safe during a physics
    // contact callback; joint creation is deferred until the world is unlocked.
    if (pendingArrow != null) {
      updateFlightRope(point);
    }
    pendingArrow = null;
    pendingAnchorBody = anchorBody;
    pendingAnchorPoint = point.cpy();
    // Once the grapple actually activates, the cooldown no longer applies - you should be free to
    // detach and fire straight back off to chain swings. The cooldown only exists to stop the shot
    // itself being spammed for free while it's still missing.
    cooldownRemaining = 0f;
  }

  private void createJoint(Body anchorBody, Vector2 point) {
    // Capture the player's normal drag once for the whole grapple cycle. Bend changes rebuild the
    // joint and must not overwrite this with SWING_DAMPING.
    preSwingLinearDamping = physicsComponent.getBody().getLinearDamping();
    originalAnchorBody = anchorBody;
    originalAnchorLocal = anchorBody.getLocalPoint(point).cpy();
    Vector2 pivot = activeContact() == null ? point : activeContact().getWorldPoint();
    totalRopeLength =
        fixedPathLength(point, ropeContacts)
            + pivot.dst(physicsComponent.getBody().getWorldCenter());
    initialRopeLength = totalRopeLength;
    rebuildJointForPath();
  }

  private Vector2 arrowPoint() {
    ArrowProjectileComponent projectile = pendingArrow.getComponent(ArrowProjectileComponent.class);
    return projectile == null ? pendingArrow.getCenterPosition() : projectile.getWorldCenter();
  }

  /** Pays out rope freely while retaining bends formed by the arrow's path around terrain. */
  private void updateFlightRope(Vector2 point) {
    World world = ServiceLocator.getPhysicsService().getPhysics().getWorld();
    Vector2 player = physicsComponent.getBody().getWorldCenter().cpy();
    removeUnwrappedContacts(world, point, player);
    insertMissingContacts(world, point, player);
  }

  /** Drops only the rope belonging to this arrow, including when it misses or expires. */
  void onArrowRemoved(Entity arrow) {
    if (pendingArrow == arrow) {
      pendingArrow = null;
      ropeContacts.clear();
    }
  }

  private void cancelFlight() {
    Entity arrow = pendingArrow;
    pendingArrow = null;
    if (arrow != null) {
      GrappleArrowComponent grappleArrow = arrow.getComponent(GrappleArrowComponent.class);
      if (grappleArrow != null) {
        grappleArrow.cancel();
      }
      if (ServiceLocator.getEntityService() != null) {
        ServiceLocator.getEntityService().scheduleRemoval(arrow);
      }
    }
    ropeContacts.clear();
  }

  private void createJointAt(Body anchorBody, Vector2 point, float length) {
    Body playerBody = physicsComponent.getBody();

    RopeJointDef def = new RopeJointDef();
    def.bodyA = anchorBody;
    def.bodyB = playerBody;

    // Convert world anchor coordinates to the anchor body's local space
    def.localAnchorA.set(anchorBody.getLocalPoint(point));

    // Pivot from the player's centre of mass so the pendulum hangs evenly
    def.localAnchorB.set(playerBody.getLocalCenter());

    // A rope only caps how far away the player can get, so momentum still carries them round the
    // anchor when it's taut. It goes slack once they're closer - unlike a rigid rod, which would
    // hold them at this exact distance and swing them in an arc even when the anchor is below.
    def.maxLength = Math.max(length, MIN_JOINT_LENGTH);
    def.collideConnected = true;

    ropeJoint =
        (RopeJoint) ServiceLocator.getPhysicsService().getPhysics().getWorld().createJoint(def);

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
      ropeJoint.setMaxLength(Math.max(remainingRopeLength(anchor), MIN_JOINT_LENGTH));
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
        // A crossed bend may still be blocked by another segment or obstacle. Remember the
        // unwrap attempt until its bypass clears, even if the endpoints cross back meanwhile.
        // Retain the winding guard: a clear chord alone could cut through a wrap around a diamond.
        contact.unwrapPending |= contact.hasCrossedSide(before, after);
        if (contact.unwrapPending && findObstruction(world, before, after) == null) {
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
    while (nextIndex <= ropeContacts.size()
        && attempts++ < MAX_ROPE_CONTACTS
        && ropeContacts.size() < MAX_ROPE_CONTACTS) {
      Vector2 after =
          nextIndex == ropeContacts.size() ? player : ropeContacts.get(nextIndex).getWorldPoint();
      RopeRayHit obstruction = findObstruction(world, before, after);
      if (obstruction == null) {
        before = after;
      } else {
        Vector2 point = findNextContact(world, obstruction, before, after, ropeContacts);
        if (!insertContactWithinRopeLength(obstruction, before, after, point, nextIndex, anchor)) {
          break;
        }
        before = point;
      }
      nextIndex++;
    }
  }

  private boolean insertContactWithinRopeLength(
      RopeRayHit obstruction,
      Vector2 before,
      Vector2 after,
      Vector2 point,
      int index,
      Vector2 anchor) {
    if (point == null) {
      return false;
    }
    ropeContacts.add(
        index, new RopeContact(obstruction.fixture, point, sideOf(before, after, point)));
    if (pendingArrow == null
        && fixedPathLength(anchor, ropeContacts) > totalRopeLength - MIN_JOINT_LENGTH) {
      // This bend would consume more rope than exists and leave no valid player constraint.
      ropeContacts.remove(index);
      return false;
    }
    return true;
  }

  private void rebuildJointForPath() {
    if (ropeJoint != null) {
      ServiceLocator.getPhysicsService().getPhysics().getWorld().destroyJoint(ropeJoint);
      ropeJoint = null;
    }

    Vector2 anchor = getOriginalAnchorPoint();
    RopeContact activeContact = activeContact();
    if (activeContact == null) {
      createJointAt(originalAnchorBody, anchor, totalRopeLength);
      return;
    }

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
      Vector2 next =
          obstruction == null
              ? null
              : findNextContact(world, obstruction, cursor, player, contacts);
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
    pendingAnchorBody = null;
    pendingAnchorPoint = null;
    cancelFlight();
    if (isAttached()) {
      ServiceLocator.getPhysicsService().getPhysics().getWorld().destroyJoint(ropeJoint);
      forgetJoint();
    }
  }

  @Override
  public void dispose() {
    // The physics world disposes its own joints on teardown, and destroying the player body
    // takes this joint with it, so just drop the reference.
    ropeJoint = null;
    cornerClimb = null;
    pendingAnchorBody = null;
    pendingAnchorPoint = null;
    cancelFlight();
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

    // Push along the arc, towards whichever side the key points. A fixed 90 degree turn of r would
    // point the wrong way once the player is above the anchor, shoving them against the key.
    Vector2 tangent = new Vector2(-r.y, r.x).nor();
    boolean pointsTheWrongWay =
        Math.abs(tangent.x) > MIN_TANGENT_X ? tangent.x * direction < 0 : direction < 0;
    if (pointsTheWrongWay) {
      tangent.scl(-1f);
    }

    body.applyForceToCenter(tangent.scl(SWING_FORCE * body.getMass()), true);
  }

  /** Starts retracting the rope while the climb control is held. */
  public void startClimbing() {
    climbing = true;
  }

  /** Stops retracting the rope and clears any unapplied constraint adjustment. */
  public void stopClimbing() {
    climbing = false;
    cornerClimb = null;
    syncRopeLengthToActualPath();
  }

  /** Starts extending the rope while the descend control is held. */
  public void startDescending() {
    descending = true;
    cornerClimb = null;
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
    CornerClimb target = cornerClimb == null ? cornerClimbAt(activeContact()) : cornerClimb;
    CornerClearance clearance = cornerClearance(target);
    if (climbing && clearance != null && climbAroundCorner(target, clearance)) {
      // Let the collider move out from under an edge before pulling it upwards. A taut,
      // shrinking segment would pin it against the terrain even with a sideways impulse.
      setTotalRopeLength(actualTotalLength + adjustment, fixedLength);
      return;
    }
    setTotalRopeLength(actualTotalLength + (descending ? adjustment : -adjustment), fixedLength);
  }

  /** Finishes clearing a corner independently of whether the rope has already unwrapped it. */
  private boolean climbAroundCorner(CornerClimb target, CornerClearance clearance) {
    Vector2 clearancePoint = clearance.point();
    Body body = physicsComponent.getBody();
    Vector2 pivot = target.body().getWorldPoint(target.localCorner());
    float clearanceRadius = pivot.dst(clearancePoint);
    float approachDistance =
        Math.max(MIN_PLAYER_SEGMENT_LENGTH, clearanceRadius)
            + clearanceRadius
            + CORNER_APPROACH_DISTANCE;
    if (cornerClimb == null && body.getWorldCenter().dst(pivot) > approachDistance) {
      return false;
    }
    float dt = ServiceLocator.getTimeSource().getDeltaTime();
    if (dt <= 0f) {
      return false;
    }
    Vector2 velocity = clearancePoint.cpy().sub(body.getWorldCenter());
    float firstDistance = velocity.dot(clearance.firstNormal());
    float secondDistance = velocity.dot(clearance.secondNormal());
    // Clearing both expanded faces is sufficient; an overshoot must not keep a bend pinned.
    if (firstDistance <= ROPE_RADIUS && secondDistance <= ROPE_RADIUS) {
      cornerClimb = null;
      return false;
    }
    cornerClimb = target;
    if (firstDistance > ROPE_RADIUS && secondDistance > ROPE_RADIUS) {
      // Clear the nearer face first. Pulling diagonally up into an underside lets contact
      // friction cancel the small sideways motion needed to escape it.
      velocity.set(
          firstDistance < secondDistance ? clearance.firstNormal() : clearance.secondNormal());
      velocity.scl(Math.min(firstDistance, secondDistance));
    }
    float distance = velocity.len();
    if (distance > 0f) {
      velocity.scl(Math.min(CLIMB_SPEED / distance, 1f / dt));
    }
    velocity.add(target.body().getLinearVelocityFromWorldPoint(clearancePoint));
    // Support the player's weight only while actively climbing around this corner.
    Vector2 impulse =
        velocity
            .sub(body.getLinearVelocity())
            .mulAdd(body.getWorld().getGravity(), -body.getGravityScale() * dt)
            .scl(body.getMass());
    body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
    return true;
  }

  /** Intersection of the corner's two edge planes, expanded by the player's solid collider. */
  private record CornerClearance(Vector2 point, Vector2 firstNormal, Vector2 secondNormal) {}

  /** A movement target in terrain-local space, with no dependence on rope contact membership. */
  private record CornerClimb(
      Body body,
      Fixture fixture,
      PolygonShape shape,
      Vector2 localCorner,
      Vector2 firstLocalNormal,
      Vector2 secondLocalNormal) {}

  private CornerClimb cornerClimbAt(RopeContact contact) {
    if (contact == null || !(contact.fixture.getShape() instanceof PolygonShape polygon)) {
      return null;
    }
    Vector2 local = new Vector2();
    Vector2 corner = new Vector2();
    int vertexIndex = 0;
    float nearest = Float.MAX_VALUE;
    Vector2 pivot = contact.getWorldPoint();
    for (int i = 0; i < polygon.getVertexCount(); i++) {
      polygon.getVertex(i, local);
      Vector2 vertex = contact.body.getWorldPoint(local);
      float distance = vertex.dst2(pivot);
      if (distance < nearest) {
        nearest = distance;
        corner.set(local);
        vertexIndex = i;
      }
    }
    int count = polygon.getVertexCount();
    polygon.getVertex((vertexIndex + count - 1) % count, local);
    Vector2 incoming = corner.cpy().sub(local);
    polygon.getVertex((vertexIndex + 1) % count, local);
    Vector2 outgoing = local.cpy().sub(corner);
    // Box2D polygon vertices wind counterclockwise, so right-hand edge normals point outwards.
    Vector2 firstNormal = new Vector2(incoming.y, -incoming.x).nor();
    Vector2 secondNormal = new Vector2(outgoing.y, -outgoing.x).nor();
    return new CornerClimb(
        contact.body, contact.fixture, polygon, corner, firstNormal, secondNormal);
  }

  private CornerClearance cornerClearance() {
    return cornerClearance(cornerClimbAt(activeContact()));
  }

  private CornerClearance cornerClearance(CornerClimb target) {
    if (target == null) {
      return null;
    }
    Vector2 corner = target.body().getWorldPoint(target.localCorner()).cpy();
    Vector2 firstNormal = target.body().getWorldVector(target.firstLocalNormal()).cpy();
    Vector2 secondNormal = target.body().getWorldVector(target.secondLocalNormal()).cpy();
    float determinant = firstNormal.crs(secondNormal);
    if (Math.abs(determinant) <= SIDE_EPSILON) {
      return null;
    }
    float firstDistance = playerExtentAlong(firstNormal) + CORNER_CLEARANCE;
    float secondDistance = playerExtentAlong(secondNormal) + CORNER_CLEARANCE;
    corner.add(
        (firstDistance * secondNormal.y - firstNormal.y * secondDistance) / determinant,
        (firstNormal.x * secondDistance - firstDistance * secondNormal.x) / determinant);
    return new CornerClearance(corner, firstNormal, secondNormal);
  }

  /**
   * Distance from the centre to the collider face nearest the corner, excluding sensor hitboxes.
   */
  private float playerExtentAlong(Vector2 outwardNormal) {
    Body body = physicsComponent.getBody();
    Vector2 local = new Vector2();
    float extent = 0f;
    for (Fixture fixture : body.getFixtureList()) {
      if (fixture.isSensor()) {
        continue;
      }
      if (fixture.getShape() instanceof PolygonShape polygon) {
        for (int i = 0; i < polygon.getVertexCount(); i++) {
          polygon.getVertex(i, local);
          extent =
              Math.max(
                  extent,
                  -body.getWorldPoint(local).cpy().sub(body.getWorldCenter()).dot(outwardNormal));
        }
      } else if (fixture.getShape() instanceof CircleShape circle) {
        float distance =
            body.getWorldPoint(circle.getPosition())
                        .cpy()
                        .sub(body.getWorldCenter())
                        .dot(outwardNormal)
                    * -1f
                + circle.getRadius();
        extent = Math.max(extent, distance);
      }
    }
    return extent;
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
    CornerClearance clearance = cornerClearance();
    float minimumSegmentLength =
        clearance == null
            ? MIN_PLAYER_SEGMENT_LENGTH
            : Math.max(
                MIN_PLAYER_SEGMENT_LENGTH,
                ropeJoint.getAnchorA().dst(clearance.point()) + ROPE_RADIUS);
    float minimumLength = Math.min(initialRopeLength, fixedLength + minimumSegmentLength);
    totalRopeLength = Math.clamp(requestedLength, minimumLength, initialRopeLength);
    ropeJoint.setMaxLength(Math.max(totalRopeLength - fixedLength, MIN_JOINT_LENGTH));
  }

  public boolean isAttached() {
    return ropeJoint != null;
  }

  /**
   * @return true from the moment a shot is fired until its cooldown ends, covering the arrow's
   *     flight - used to hide the aim preview the instant you actually shoot
   */
  public boolean isOnCooldown() {
    return cooldownRemaining > 0f;
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
   * @return ordered world-space points from player to the flying arrow or terrain anchor.
   */
  public List<Vector2> getRopePath() {
    List<Vector2> points = new ArrayList<>(3);
    Vector2 anchor =
        pendingArrow != null
            ? arrowPoint()
            : pendingAnchorPoint != null ? pendingAnchorPoint.cpy() : getOriginalAnchorPoint();
    if (anchor == null) {
      return points;
    }
    points.add(physicsComponent.getBody().getWorldCenter().cpy());
    for (int i = ropeContacts.size() - 1; i >= 0; i--) {
      points.add(ropeContacts.get(i).getWorldPoint());
    }
    points.add(anchor);
    return points;
  }

  static class RopeContact {
    private final Fixture fixture;
    private final Body body;
    private final Vector2 localPoint;
    private final int initialSide;
    private boolean unwrapPending;

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
