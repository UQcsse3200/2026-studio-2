package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

/** Fires a grapple arrow, then swings from wherever it lands. */
public class GrappleComponent extends Component {
  private static final float MAX_RANGE = 8f;
  private static final float GRAPPLE_COOLDOWN = 2f;
  private static final float SWING_FORCE = 5f;
  private static final float MAX_SWING_SPEED = 7f;
  private static final float SWING_DAMPING = 0.5f;
  private static final float RELEASE_DAMPING = 2f;

  private PhysicsComponent physicsComponent;
  private InventoryComponent inventory;
  private DistanceJoint ropeJoint;
  private Body anchorBody;
  private Vector2 anchorPoint;
  private RaycastHit raycastHit;
  private float cooldownRemaining;

  // Box2D locks the world during a step, so attachments are queued and built next frame.
  private Body pendingAnchorBody;
  private Vector2 pendingAnchorPoint;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    inventory = entity.getComponent(InventoryComponent.class);
    entity.getEvents().addListener("grappleFire", this::fire);
    entity.getEvents().addListener("grappleRelease", this::release);
    entity.getEvents().addListener("grappleSwing", this::swing);
    entity.getEvents().addListener("grappleResponse", this::handleSuccessfulFire);
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

  /**
   * Validates and requests a grapple location before launching a grapple projectile.
   *
   * @param direction direction in which to fire
   */
  void fire(Vector2 direction) {
    if (inventory == null || !inventory.hasItem(ItemType.RopeArrow)) {
      entity.getEvents().trigger("itemUseFailed", ItemType.RopeArrow);
      return;
    }
    if (direction == null
        || direction.isZero()
        || cooldownRemaining > 0f
        || isAttached()
        || raycastHit != null) {
      return;
    }

    Vector2 start = entity.getCenterPosition();
    Vector2 end = start.cpy().mulAdd(direction.cpy().nor(), MAX_RANGE);
    RaycastHit hit = new RaycastHit();

    if (!ServiceLocator.getPhysicsService()
        .getPhysics()
        .raycast(start, end, PhysicsLayer.SOLID, hit)) {
      return;
    }

    raycastHit = hit;
    entity.getEvents().trigger("grappleRequested", hit.point.cpy());
  }

  /**
   * Launches the grapple projectile after the game area confirms that the requested location is a
   * valid platform grapple point.
   *
   * @param success whether the requested grapple point was approved
   */
  void handleSuccessfulFire(boolean success) {
    if (!success || raycastHit == null) {
      raycastHit = null;
      return;
    }

    Vector2 start = entity.getCenterPosition();
    Vector2 aim = raycastHit.point.cpy().sub(start).nor();
    Vector2 spawn = start.cpy().mulAdd(aim, entity.getScale().x * 0.6f);

    Entity arrow = ProjectileFactory.createGrappleArrow(spawn, aim);
    arrow.addComponent(new GrappleArrowComponent(entity));
    ServiceLocator.getEntityService().register(arrow);
    cooldownRemaining = GRAPPLE_COOLDOWN;
    raycastHit = null;
  }

  /**
   * Queues a rope attachment. The grapple arrow calls this during a physics step, so the joint is
   * built during the next entity update.
   *
   * @param anchorBody the body that was hit
   * @param point where the arrow struck, in world coordinates
   */
  public void attachTo(Body anchorBody, Vector2 point) {
    if (isAttached() || pendingAnchorBody != null || anchorBody == null || point == null) {
      return;
    }
    pendingAnchorBody = anchorBody;
    pendingAnchorPoint = point.cpy();
  }

  private void createJoint(Body anchorBody, Vector2 point) {
    anchorPoint = point.cpy();
    this.anchorBody = anchorBody;
    Body playerBody = physicsComponent.getBody();

    DistanceJointDef def = new DistanceJointDef();
    def.bodyA = anchorBody;
    def.bodyB = playerBody;
    def.localAnchorA.set(anchorBody.getLocalPoint(anchorPoint));
    def.localAnchorB.set(playerBody.getLocalCenter());
    def.length = entity.getCenterPosition().dst(anchorPoint);
    def.frequencyHz = 0f;
    def.dampingRatio = 0f;
    def.collideConnected = true;

    ropeJoint =
        (DistanceJoint) ServiceLocator.getPhysicsService().getPhysics().getWorld().createJoint(def);

    playerBody.setFixedRotation(true);
    playerBody.setLinearDamping(SWING_DAMPING);
  }

  /** Detaches the rope, restoring free movement while retaining swing momentum. */
  public void release() {
    if (!isAttached()) {
      return;
    }
    ServiceLocator.getPhysicsService().getPhysics().getWorld().destroyJoint(ropeJoint);
    ropeJoint = null;
    anchorBody = null;
    anchorPoint = null;
    physicsComponent.getBody().setLinearDamping(RELEASE_DAMPING);
  }

  /** Pushes along the swing arc while anchored. */
  void swing(float direction) {
    if (!isAttached() || direction == 0f) {
      return;
    }

    Body body = physicsComponent.getBody();
    if (body.getLinearVelocity().len() > MAX_SWING_SPEED) {
      return;
    }

    Vector2 r = entity.getCenterPosition().sub(anchorPoint);
    Vector2 tangent = direction > 0 ? new Vector2(-r.y, r.x).nor() : new Vector2(r.y, -r.x).nor();
    body.applyForceToCenter(tangent.scl(SWING_FORCE * body.getMass()), true);
  }

  public boolean isAttached() {
    return ropeJoint != null;
  }

  public Vector2 getAnchorPoint() {
    if (!isAttached() || anchorBody == null) {
      return null;
    }
    return anchorBody.getWorldPoint(ropeJoint.getLocalAnchorA());
  }
}
