package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.ArrowRenderComponent;

/** Factory for player and enemy projectile entities. */
public class ProjectileFactory {
  public static final int STANDARD_ARROW_DAMAGE = 10;
  public static final float STANDARD_ARROW_SPEED = 15f;
  public static final float STANDARD_ARROW_RANGE = 15f;

  /**
   * Creates a standard player arrow with unlimited-ammo behaviour.
   *
   * @param position projectile spawn position
   * @param direction projectile travel direction
   * @return unregistered arrow entity
   */
  public static Entity createPlayerArrow(Vector2 position, Vector2 direction) {
    return createPlayerArrow(position, direction, ItemType.ARROW, 0f, 0f);
  }

  public static Entity createPlayerArrow(
      Vector2 position, Vector2 direction, float poisonDps, float poisonDuration) {
    return createPlayerArrow(position, direction, ItemType.ARROW, poisonDps, poisonDuration);
  }

  public static Entity createPlayerArrow(
      Vector2 position,
      Vector2 direction,
      ItemType arrowType,
      float poisonDps,
      float poisonDuration) {

    Entity arrow =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER_PROJECTILE))
            .addComponent(new CombatStatsComponent(1, arrowType.getDamage()))
            .addComponent(
                new ArrowProjectileComponent(
                    direction,
                    STANDARD_ARROW_SPEED,
                    arrowType.getRange(),
                    arrowType.getBurnDamagePerSecond(),
                    arrowType.getBurnTime(),
                    arrowType.getSlowSpeed(),
                    arrowType.getSlowTime(),
                    poisonDps,
                    poisonDuration))
            .addComponent(new ArrowRenderComponent(arrowType.getProjectileTexturePath()));

    arrow.setScale(0.6f, 0.2f);
    arrow.setPosition(position.x - arrow.getScale().x / 2f, position.y - arrow.getScale().y / 2f);
    return arrow;
  }

  private ProjectileFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
