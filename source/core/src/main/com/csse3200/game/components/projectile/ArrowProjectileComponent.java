package com.csse3200.game.components.projectile;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

/** Moves a projectile, handles parabolic flight, and triggers elemental status effects. */
public class ArrowProjectileComponent extends Component {

  private static final short TARGET_LAYERS = PhysicsLayer.NPC;
  private static final short TERRAIN = (short) (PhysicsLayer.GROUND | PhysicsLayer.OBSTACLE);
  private static final float ARC_GRAVITY_SCALE = 0.4f;

  private final Entity shooter;
  private final Vector2 direction;
  private final float speed;
  private final float maximumRange;
  private final ArrowType arrowType;

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
    if (body.getPosition().dst2(startPosition) >= maximumRange * maximumRange) {
      expire();
      return;
    }

    updateRotation(body);
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

    // Ignore collisions with the shooter entity
    Object userData = other.getBody().getUserData();
    if (userData instanceof BodyUserData) {
      Entity hitEntity = ((BodyUserData) userData).entity;
      if (hitEntity != null && hitEntity == shooter) {
        return;
      }
    }

    short otherLayer = other.getFilterData().categoryBits;

    // Ignore all player layer collisions
    if (PhysicsLayer.contains(PhysicsLayer.PLAYER, otherLayer)) {
      return;
    }

    if (PhysicsLayer.contains(TARGET_LAYERS, otherLayer)) {
      damageTarget(other);
      expire();
    } else if (PhysicsLayer.contains(TERRAIN, otherLayer)) {
      expire();
    }
  }

  private void damageTarget(Fixture other) {
    Object userData = other.getBody().getUserData();
    if (!(userData instanceof BodyUserData)) {
      return;
    }
    Entity target = ((BodyUserData) userData).entity;
    if (target == null || target == entity) {
      return;
    }

    if (combatStats != null && target.getComponent(CombatStatsComponent.class) != null) {
      target.getEvents().trigger("takeDamage", combatStats);
    }

    switch (arrowType) {
      case COLD:
        target.getEvents().trigger("applyCold", 0.5f, 3.0f);
        target.getEvents().trigger("slow", 0.5f);
        break;

      case FIRE:
        target.getEvents().trigger("applyFire", 5, 3.0f);
        target.getEvents().trigger("burn", 5);
        break;

      default:
        break;
    }
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
