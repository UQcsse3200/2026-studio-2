package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.ProjectileComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/** Factory for creating enemy projectiles. */
public class ProjectileFact {

  /**
   * Creates a projectile that travels towards a target position.
   *
   * @param targetPosition position the projectile travels towards
   * @param damage damage dealt when the projectile hits the player
   * @param speed projectile movement speed
   * @param lifetime maximum projectile lifetime in seconds
   * @return projectile entity
   */
  public static Entity createEnemyProjectile(
      Vector2 targetPosition, int damage, float speed, float lifetime) {

    PhysicsMovementComponent movement = new PhysicsMovementComponent(new Vector2(speed, speed));
    movement.setTarget(targetPosition);

    Entity projectile =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(movement)
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
            .addComponent(new CombatStatsComponent(1, damage))
            .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER))
            .addComponent(new ProjectileComponent(lifetime))
            .addComponent(new ColliderComponent())
            .addComponent(new TextureRenderComponent("images/arrow.png"));

    projectile.getComponent(TextureRenderComponent.class).scaleEntity();
    projectile.setScale(projectile.getScale().scl(2f));
    PhysicsUtils.setScaledCollider(projectile, 0.3f, 0.3f);

    return projectile;
  }

  private ProjectileFact() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
