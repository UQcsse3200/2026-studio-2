package com.csse3200.game.components;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.physics.PhysicsLayer;
// import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.services.ServiceLocator;

/** Controls the lifetime and disposal behaviour of a projectile. */
public class ProjectileComponent extends Component {
  private float remainingLifetime;
  // private HitboxComponent hitboxComponent;
  private PhysicsMovementComponent movementComponent;
  private float previousDistanceToTarget = Float.MAX_VALUE;
  private float timeAlive = 0f;
  private static final float ARM_TIME = 0.15f;

  /**
   * Creates a projectile component.
   *
   * @param lifetime maximum lifetime of the projectile in seconds
   */
  public ProjectileComponent(float lifetime) {
    this.remainingLifetime = lifetime;
  }

  @Override
  public void create() {
    // hitboxComponent = entity.getComponent(HitboxComponent.class);
    movementComponent = entity.getComponent(PhysicsMovementComponent.class);
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  @Override
  public void update() {
    float deltaTime = ServiceLocator.getTimeSource().getDeltaTime();
    remainingLifetime -= deltaTime;
    timeAlive += deltaTime;

    if (remainingLifetime <= 0f) {
      ServiceLocator.getEntityService().scheduleForDisposal(entity);
      return;
    }

    if (movementComponent != null && movementComponent.getTarget() != null) {
      float distanceToTarget = entity.getCenterPosition().dst(movementComponent.getTarget());

      // Remove the projectile once it reaches or passes its target.
      if (timeAlive >= ARM_TIME
          && (distanceToTarget < 0.25f || distanceToTarget > previousDistanceToTarget)) {
        ServiceLocator.getEntityService().scheduleForDisposal(entity);
        return;
      }

      previousDistanceToTarget = distanceToTarget;
    }
  }

  /**
   * Despawns the projectile when it collides with the player.
   *
   * @param me projectile fixture
   * @param other fixture belonging to the collided entity
   */
  private void onCollisionStart(Fixture me, Fixture other) {

    if (timeAlive < ARM_TIME) {
      return;
    }

    short otherLayer = other.getFilterData().categoryBits;

    boolean hitPlayer = PhysicsLayer.contains(PhysicsLayer.PLAYER, otherLayer);

    boolean hitSolid = PhysicsLayer.contains(PhysicsLayer.SOLID, otherLayer);

    if (hitPlayer || hitSolid) {
      ServiceLocator.getEntityService().scheduleForDisposal(entity);
    }
  }
}
