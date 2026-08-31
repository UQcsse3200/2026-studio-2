package com.csse3200.game.components.projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

/** Moves a player arrow, applies damage on impact, and removes it when spent. */
public class ArrowProjectileComponent extends Component {
  private static final short TERRAIN = (short) (PhysicsLayer.GROUND | PhysicsLayer.OBSTACLE);

  private final Vector2 direction;
  private final float speed;
  private final float maximumRange;

  private PhysicsComponent physicsComponent;
  private HitboxComponent hitboxComponent;
  private CombatStatsComponent combatStats;
  private Vector2 startPosition;
  private boolean spent;

  /**
   * Creates a straight-moving arrow projectile.
   *
   * @param direction normalized travel direction
   * @param speed travel speed in world units per second
   * @param maximumRange maximum travel distance in world units
   */
  public ArrowProjectileComponent(Vector2 direction, float speed, float maximumRange) {
    if (direction == null || direction.isZero()) {
      throw new IllegalArgumentException("Arrow direction must not be zero");
    }
    if (speed <= 0f || maximumRange <= 0f) {
      throw new IllegalArgumentException("Arrow speed and range must be positive");
    }
    this.direction = direction.cpy().nor();
    this.speed = speed;
    this.maximumRange = maximumRange;
  }

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    hitboxComponent = entity.getComponent(HitboxComponent.class);
    combatStats = entity.getComponent(CombatStatsComponent.class);

    Body body = physicsComponent.getBody();
    body.setGravityScale(0.05f);
    body.setLinearDamping(0f);
    body.setBullet(true);
    body.setLinearVelocity(direction.cpy().scl(speed));
    startPosition = body.getPosition().cpy();

    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  @Override
  public void update() {
    if (!spent
        && physicsComponent.getBody().getPosition().dst2(startPosition)
            >= maximumRange * maximumRange) {
      expire();
    }
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    if (spent || hitboxComponent.getFixture() != me) {
      return;
    }

    short otherLayer = other.getFilterData().categoryBits;
    if (PhysicsLayer.contains(PhysicsLayer.NPC, otherLayer)) {
      damageTarget(other);
      expire();
    } else if (PhysicsLayer.contains(TERRAIN, otherLayer)) {
      expire();
    }
  }

  private void damageTarget(Fixture other) {
    // Non-damaging arrows (e.g. grapple) have no combat stats
    if (combatStats == null) {
      return;
    }
    Object userData = other.getBody().getUserData();
    if (!(userData instanceof BodyUserData)) {
      return;
    }
    Entity target = ((BodyUserData) userData).entity;
    if (target == null) {
      return;
    }
    CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
    if (targetStats != null) {
      targetStats.hit(combatStats);
    }
  }

  private void expire() {
    if (spent) {
      return;
    }
    spent = true;
    ServiceLocator.getEntityService().scheduleRemoval(entity);
  }

  /**
   * @return whether this projectile has hit something or exceeded its range
   */
  public boolean isSpent() {
    return spent;
  }

  /**
   * @return normalized arrow travel direction
   */
  public Vector2 getDirection() {
    return direction.cpy();
  }
}
