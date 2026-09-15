package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.PoisonBuff;
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
  public static final float STANDARD_ARROW_RANGE = 50f;

  public static final int ICE_ARROW_DAMAGE = 8;
  public static final float ICE_ARROW_SPEED = 16f;
  public static final float ICE_ARROW_RANGE = 50f;

  public static final int FIRE_ARROW_DAMAGE = 12;
  public static final float FIRE_ARROW_SPEED = 18f;
  public static final float FIRE_ARROW_RANGE = 50f;

  public static final float GRAPPLE_ARROW_SPEED = 22f;
  public static final float GRAPPLE_ARROW_RANGE = 50f;

  public static Entity createPlayerArrow(Vector2 position, Vector2 direction) {
    return createPlayerArrow(null, position, direction);
  }

  public static Entity createIceArrow(Vector2 position, Vector2 direction) {
    return createIceArrow(null, position, direction);
  }

  public static Entity createFireArrow(Vector2 position, Vector2 direction) {
    return createFireArrow(null, position, direction);
  }

  public static Entity createGrappleArrow(Vector2 position, Vector2 direction) {
    return createGrappleArrow(null, position, direction);
  }

  public static Entity createPoisonArrow(Vector2 position, Vector2 direction) {
    return createPoisonArrow(null, position, direction);
  }

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

  public static Entity createIceArrow(Entity shooter, Vector2 position, Vector2 direction) {
    return createArrow(
        shooter,
        position,
        direction,
        ICE_ARROW_DAMAGE,
        ICE_ARROW_SPEED,
        ICE_ARROW_RANGE,
        ArrowType.ICE);
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
    return createArrow(
        shooter,
        position,
        direction,
        0,
        GRAPPLE_ARROW_SPEED,
        GRAPPLE_ARROW_RANGE,
        ArrowType.GRAPPLE);
  }

  public static Entity createPoisonArrow(Entity shooter, Vector2 position, Vector2 direction) {
    return createArrow(
        shooter,
        position,
        direction,
        STANDARD_ARROW_DAMAGE,
        STANDARD_ARROW_SPEED,
        STANDARD_ARROW_RANGE,
        ArrowType.POISON);
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
    PoisonBuff poisonBuff = shooter != null ? shooter.getComponent(PoisonBuff.class) : null;
    float poisonDps = poisonBuff != null ? poisonBuff.getPoisonDamagePerSecond() : 0f;
    float poisonDuration = poisonBuff != null ? poisonBuff.getPoisonDuration() : 0f;

    Entity arrow =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.DynamicBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER_PROJECTILE))
            .addComponent(new CombatStatsComponent(1, damage))
            .addComponent(
                new ArrowProjectileComponent(
                    shooter, normalizedDir, speed, range, arrowType, poisonDps, poisonDuration))
            .addComponent(new ArrowRenderComponent(arrowType));

    arrow.setScale(0.6f, 0.2f);
    arrow.setPosition(position.x - arrow.getScale().x / 2f, position.y - arrow.getScale().y / 2f);

    return arrow;
  }

  private ProjectileFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
