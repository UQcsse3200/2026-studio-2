package com.csse3200.game.components.projectile;

import com.badlogic.gdx.math.MathUtils;
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
  private static final short TERRAIN = (short) (PhysicsLayer.GROUND | PhysicsLayer.OBSTACLE);
  private static final float ARC_GRAVITY_SCALE = 0.4f;
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
    body.setGravityScale(arrowType == ArrowType.GRAPPLE ? 0f : ARC_GRAVITY_SCALE);
    body.setLinearDamping(0f);
    body.setBullet(true);
    body.setLinearVelocity(direction.cpy().scl(speed));
    ignorePlayerCollisions(body);
    startPosition = body.getPosition().cpy();

    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  /** Lets a fired arrow pass through the player instead of shoving them. */
  private void ignorePlayerCollisions(Body body) {
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
    if (body.getPosition().dst2(origin) >= maximumRange * maximumRange) {
      logger.info(
          "{} arrow hit max range: origin={} ({}) arrowPos={} distance={} maxRange={}",
          arrowType,
          origin,
          shooter != null ? "live shooter position" : "spawn point, no shooter given",
          body.getPosition(),
          body.getPosition().dst(origin),
          maximumRange);
      expire();
      return;
    }

    updateRotation(body);
  }

  private Vector2 rangeOrigin() {
    return shooter != null ? shooter.getCenterPosition() : startPosition;
  }

  private void updateRotation(Body body) {
    Vector2 velocity = body.getLinearVelocity();
    if (velocity.isZero()) {
      return;
    }
    float angleDeg = velocity.angleDeg();
    body.setTransform(body.getPosition(), angleDeg * MathUtils.degreesToRadians);
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    if (spent || hitboxComponent == null || hitboxComponent.getFixture() != me) {
      return;
    }

    if (arrowType == ArrowType.GRAPPLE) {
      return;
    }

    Object userData = other.getBody().getUserData();
    if (userData instanceof BodyUserData) {
      Entity hitEntity = ((BodyUserData) userData).entity;
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
    return physicsComponent.getBody().getPosition().dst2(startPosition) > MIN_TRAVEL * MIN_TRAVEL;
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
