package com.csse3200.game.components;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Controls the lifetime and disposal behaviour of a projectile.
 */
public class ProjectileComponent extends Component {
  private float remainingLifetime;
  private HitboxComponent hitboxComponent;

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
    hitboxComponent = entity.getComponent(HitboxComponent.class);
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  @Override
  public void update() {
    remainingLifetime -= ServiceLocator.getTimeSource().getDeltaTime();

    if (remainingLifetime <= 0f) {
      ServiceLocator.getEntityService().scheduleForDisposal(entity);
    }
  }

  /**
   * Despawns the projectile when it collides with the player.
   *
   * @param me projectile fixture
   * @param other fixture belonging to the collided entity
   */
  private void onCollisionStart(Fixture me, Fixture other) {
    if (hitboxComponent == null || hitboxComponent.getFixture() != me) {
      return;
    }

    if (!PhysicsLayer.contains(
        PhysicsLayer.PLAYER, other.getFilterData().categoryBits)) {
      return;
    }

    ServiceLocator.getEntityService().scheduleForDisposal(entity);
  }
}
