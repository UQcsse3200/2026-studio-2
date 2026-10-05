package com.csse3200.game.components.projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Moves a projectile, handles parabolic flight, and triggers elemental status effects. */
public class ArrowProjectileComponent extends Component {

  private static final Logger logger = LoggerFactory.getLogger(ArrowProjectileComponent.class);
  private static final short TARGET_LAYERS = PhysicsLayer.NPC;

  /** Layers an arrow stops against. Public so aiming previews can stop at the same surfaces. */
  public static final short TERRAIN = (short) (PhysicsLayer.GROUND | PhysicsLayer.OBSTACLE);

  /** How much of the world's gravity every arrow (grapple included) falls under in flight. */
  public static final float ARC_GRAVITY_SCALE = 0.4f;

  private static final float MIN_TRAVEL = 0.5f;

  private final Entity shooter;
  private final Vector2 direction;
  private final float speed;
  private final float maximumRange;
  private final ArrowType arrowType;
  private final float poisonDamagePerSecond;
  private final float poisonDuration;

  private PhysicsComponent physicsComponent;
  private HitboxComponent hitboxComponent;
  private CombatStatsComponent combatStats;
  private Vector2 startPosition;
  private boolean spent;

  public ArrowProjectileComponent(Vector2 direction, float speed, float maximumRange) {
    this(null, direction, speed, maximumRange, ArrowType.STANDARD);
  }

  public ArrowProjectileComponent(
      Vector2 direction, float speed, float maximumRange, ArrowType arrowType) {
    this(null, direction, speed, maximumRange, arrowType);
  }

  public ArrowProjectileComponent(
      Entity shooter, Vector2 direction, float speed, float maximumRange, ArrowType arrowType) {
    this(shooter, direction, speed, maximumRange, arrowType, 0f, 0f);
  }

  public ArrowProjectileComponent(
      Entity shooter,
      Vector2 direction,
      float speed,
      float maximumRange,
      ArrowType arrowType,
      float poisonDamagePerSecond,
      float poisonDuration) {
    if (direction == null || direction.isZero()) {
      throw new IllegalArgumentException("Arrow direction must not be zero");
    }

    if (speed <= 0f || maximumRange <= 0f) {
      throw new IllegalArgumentException("Arrow speed and range must be positive");
    }
    this.shooter = shooter;
    this.direction = direction.cpy().nor();
    this.speed = speed;
    this.maximumRange = maximumRange;
    this.arrowType = arrowType != null ? arrowType : ArrowType.STANDARD;
    this.poisonDamagePerSecond = poisonDamagePerSecond;
    this.poisonDuration = poisonDuration;
  }

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    hitboxComponent = entity.getComponent(HitboxComponent.class);
    combatStats = entity.getComponent(CombatStatsComponent.class);

    Body body = physicsComponent.getBody();
    body.setFixedRotation(true);
    body.setGravityScale(ARC_GRAVITY_SCALE);
    body.setLinearDamping(0f);
    body.setBullet(true);
    body.setLinearVelocity(direction.cpy().scl(speed));
    ignorePlayerCollisions(body);
    startPosition = getWorldCenter();

    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  /** Lets a fired arrow pass through the player instead of shoving them. */
  private void ignorePlayerCollisions(Body body) {
    // Entity component creation order is unspecified: the hitbox may not have a fixture yet.
    if (hitboxComponent != null) {
      hitboxComponent.excludeCollisionLayers(PhysicsLayer.PLAYER);
    }
    for (Fixture fixture : body.getFixtureList()) {
      Filter filter = fixture.getFilterData();
      filter.maskBits &= ~PhysicsLayer.PLAYER;
      fixture.setFilterData(filter);
    }
  }

  @Override
  public void update() {
    if (spent) {
      return;
    }

    Body body = physicsComponent.getBody();
    Vector2 origin = rangeOrigin();
    Vector2 position = getWorldCenter();
    if (position.dst2(origin) >= maximumRange * maximumRange) {
      logger.info(
          "{} arrow hit max range: origin={} ({}) arrowPos={} distance={} maxRange={}",
          arrowType,
          origin,
          shooter != null ? "live shooter position" : "spawn point, no shooter given",
          position,
          position.dst(origin),
          maximumRange);
      expire();
      return;
    }

    updateRotation(body);
  }

  private Vector2 rangeOrigin() {
    return shooter != null ? shooter.getCenterPosition() : startPosition;
  }

  /**
   * @return where the arrow actually is in the world - the centre of its collision box. This is not
   *     {@code entity.getCenterPosition()}: the body's origin is the box's corner, so once the
   *     arrow has turned to face its flight path the two no longer agree.
   */
  public Vector2 getWorldCenter() {
    if (physicsComponent == null || physicsComponent.getBody() == null) {
      return entity.getCenterPosition();
    }
    // Box2D hands back one shared vector that every later call overwrites, so take a copy.
    return physicsComponent.getBody().getWorldPoint(localCenter()).cpy();
  }

  /**
   * The centre of the arrow's collision box in the body's own space (the box starts at the origin).
   */
  private Vector2 localCenter() {
    return entity.getScale().scl(0.5f);
  }

  /**
   * Turns the arrow to face the way it's flying. Box2D rotates a body about its origin, which is
   * the corner of the arrow's box - rotating there swings the box up to ~0.6 units away from the
   * sprite, so a wall would be hit well before the arrow visibly reached it. Instead, pivot about
   * the box's own centre: keep the centre where it is and move the origin to suit.
   */
  private void updateRotation(Body body) {
    Vector2 velocity = body.getLinearVelocity();
    if (velocity.isZero()) {
      return;
    }
    float angle = velocity.angleRad();
    Vector2 origin = getWorldCenter().sub(localCenter().rotateRad(angle));
    body.setTransform(origin, angle);
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    if (spent || hitboxComponent == null || hitboxComponent.getFixture() != me) {
      return;
    }

    if (arrowType == ArrowType.GRAPPLE) {
      return;
    }

    Object userData = other.getBody().getUserData();
    if (userData instanceof BodyUserData bodyUserData) {
      Entity hitEntity = bodyUserData.entity;
      if (hitEntity != null && hitEntity == shooter) {
        return;
      }
    }

    short otherLayer = other.getFilterData().categoryBits;
    if (PhysicsLayer.contains(PhysicsLayer.PLAYER, otherLayer)) {
      return;
    }

    if (PhysicsLayer.contains(TARGET_LAYERS, otherLayer)) {
      if (damageTarget(other)) {
        expire();
      }
    } else if (PhysicsLayer.contains(TERRAIN, otherLayer) && hasClearedSpawn()) {
      expire();
    }
  }

  private boolean hasClearedSpawn() {
    return getWorldCenter().dst2(startPosition) > MIN_TRAVEL * MIN_TRAVEL;
  }

  private boolean damageTarget(Fixture other) {
    Object userData = other.getBody().getUserData();
    if (!(userData instanceof BodyUserData)) {
      return false;
    }

    Entity target = ((BodyUserData) userData).entity;
    if (target == null || target == entity) {
      return false;
    }

    CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
    if (targetStats == null || combatStats == null) {
      return false;
    }

    int healthBefore = targetStats.getHealth();
    target.getEvents().trigger("takeDamage", combatStats);
    boolean damaged = targetStats.getHealth() < healthBefore;

    switch (arrowType) {
      case FIRE:
        target
            .getEvents()
            .trigger(
                "applyBurn",
                ItemType.FIRE_ARROW.getBurnDamagePerSecond(),
                ItemType.FIRE_ARROW.getBurnTime());
        break;
      case ICE:
        target
            .getEvents()
            .trigger(
                "applySlow", ItemType.ICE_ARROW.getSlowSpeed(), ItemType.ICE_ARROW.getSlowTime());
        break;
      case POISON:
        target.getEvents().trigger("applyPoison", 5f, 3f);
        break;
      default:
        break;
    }

    if (poisonDamagePerSecond > 0f && poisonDuration > 0f) {
      target.getEvents().trigger("applyPoison", poisonDamagePerSecond, poisonDuration);
    }

    // Thrown potions deal their effect as a debuff, even when instant damage is zero.
    return damaged || arrowType == ArrowType.POTION;
  }

  private void expire() {
    if (spent) {
      return;
    }
    spent = true;
    ServiceLocator.getEntityService().scheduleRemoval(entity);
  }

  public boolean isSpent() {
    return spent;
  }

  public Vector2 getDirection() {
    return direction.cpy();
  }

  public Vector2 getCurrentDirection() {
    if (physicsComponent != null && physicsComponent.getBody() != null) {
      Vector2 vel = physicsComponent.getBody().getLinearVelocity();
      if (!vel.isZero()) {
        return vel.cpy().nor();
      }
    }
    return direction.cpy();
  }

  public ArrowType getArrowType() {
    return arrowType;
  }
}
