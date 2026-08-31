package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.ArrowRenderComponent;

/** Factory for player and enemy projectile entities. */
public class ProjectileFactory {
  public static final int STANDARD_ARROW_DAMAGE = 10;
  public static final float STANDARD_ARROW_SPEED = 12f;
  public static final float STANDARD_ARROW_RANGE = 15f;

  public static final float GRAPPLE_ARROW_SPEED = 16f;
  public static final float GRAPPLE_ARROW_RANGE = 10f;

  /**
   * Creates a standard player arrow with unlimited-ammo behaviour.
   *
   * @param position projectile spawn position
   * @param direction projectile travel direction
   * @return unregistered arrow entity
   */
  public static Entity createPlayerArrow(Vector2 position, Vector2 direction) {
    Entity arrow =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER_PROJECTILE))
            .addComponent(new CombatStatsComponent(1, STANDARD_ARROW_DAMAGE))
            .addComponent(
                new ArrowProjectileComponent(direction, STANDARD_ARROW_SPEED, STANDARD_ARROW_RANGE))
            .addComponent(new ArrowRenderComponent());
    arrow.setScale(0.5f, 0.1f);
    arrow.setPosition(position);
    return arrow;
  }

  /**
   * Creates a grapple arrow. Deals no damage, but latches onto terrain so the player can swing from
   * it. The caller attaches a GrappleArrowComponent before registering.
   *
   * @param position projectile spawn position
   * @param direction projectile travel direction
   * @return unregistered grapple arrow entity
   */
  public static Entity createGrappleArrow(Vector2 position, Vector2 direction) {
    Entity arrow =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER_PROJECTILE))
            .addComponent(
                new ArrowProjectileComponent(direction, GRAPPLE_ARROW_SPEED, GRAPPLE_ARROW_RANGE))
            .addComponent(new ArrowRenderComponent());
    arrow.setScale(0.5f, 0.1f);
    arrow.setPosition(position);
    return arrow;
  }

  private ProjectileFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
