package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.item.ArrowRenderComponent;

/** Factory for player and enemy projectile entities. */
public class ProjectileFactory {
  public static final int STANDARD_ARROW_DAMAGE = 10;
  public static final float STANDARD_ARROW_SPEED = 18f;
  public static final float STANDARD_ARROW_RANGE = 26f;

  public static final int COLD_ARROW_DAMAGE = 8;
  public static final float COLD_ARROW_SPEED = 16f;
  public static final float COLD_ARROW_RANGE = 24f;

  public static final int FIRE_ARROW_DAMAGE = 12;
  public static final float FIRE_ARROW_SPEED = 18f;
  public static final float FIRE_ARROW_RANGE = 26f;

  public static final float GRAPPLE_ARROW_SPEED = 22f;
  public static final float GRAPPLE_ARROW_RANGE = 28f;

  // Overloads
  public static Entity createPlayerArrow(Vector2 position, Vector2 direction) {
    return createPlayerArrow(null, position, direction);
  }

  public static Entity createColdArrow(Vector2 position, Vector2 direction) {
    return createColdArrow(null, position, direction);
  }

  public static Entity createFireArrow(Vector2 position, Vector2 direction) {
    return createFireArrow(null, position, direction);
  }

  public static Entity createGrappleArrow(Vector2 position, Vector2 direction) {
    return createGrappleArrow(null, position, direction);
  }

  // Factory methods with shooter
  public static Entity createPlayerArrow(Entity shooter, Vector2 position, Vector2 direction) {
    return createArrow(
        shooter,
        position,
        direction,
        STANDARD_ARROW_DAMAGE,
        STANDARD_ARROW_SPEED,
        STANDARD_ARROW_RANGE,
        ArrowType.STANDARD);
  }

  public static Entity createColdArrow(Entity shooter, Vector2 position, Vector2 direction) {
    return createArrow(
        shooter,
        position,
        direction,
        COLD_ARROW_DAMAGE,
        COLD_ARROW_SPEED,
        COLD_ARROW_RANGE,
        ArrowType.COLD);
  }

  public static Entity createFireArrow(Entity shooter, Vector2 position, Vector2 direction) {
    return createArrow(
        shooter,
        position,
        direction,
        FIRE_ARROW_DAMAGE,
        FIRE_ARROW_SPEED,
        FIRE_ARROW_RANGE,
        ArrowType.FIRE);
  }

  public static Entity createGrappleArrow(Entity shooter, Vector2 position, Vector2 direction) {
    Vector2 normalizedDir = direction.cpy().nor();

    Entity arrow =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.DynamicBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER_PROJECTILE))
            .addComponent(
                new ArrowProjectileComponent(
                    shooter,
                    normalizedDir,
                    GRAPPLE_ARROW_SPEED,
                    GRAPPLE_ARROW_RANGE,
                    ArrowType.GRAPPLE))
            .addComponent(new ArrowRenderComponent(ArrowType.GRAPPLE));

    arrow.setScale(0.6f, 0.2f);
    arrow.setPosition(position.x - arrow.getScale().x / 2f, position.y - arrow.getScale().y / 2f);

    return arrow;
  }

  private static Entity createArrow(
      Entity shooter,
      Vector2 position,
      Vector2 direction,
      int damage,
      float speed,
      float range,
      ArrowType arrowType) {
    Vector2 normalizedDir = direction.cpy().nor();

    Entity arrow =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.DynamicBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER_PROJECTILE))
            .addComponent(new CombatStatsComponent(1, damage))
            .addComponent(
                new ArrowProjectileComponent(shooter, normalizedDir, speed, range, arrowType))
            .addComponent(new ArrowRenderComponent(arrowType));

    arrow.setScale(0.6f, 0.2f);
    arrow.setPosition(position.x - arrow.getScale().x / 2f, position.y - arrow.getScale().y / 2f);

    return arrow;
  }

  private ProjectileFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
