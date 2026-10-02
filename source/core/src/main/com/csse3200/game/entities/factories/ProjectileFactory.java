package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.item.ArrowRenderComponent;

/** Factory for player and enemy projectile entities. */
public class ProjectileFactory {
  public static final float STANDARD_ARROW_SPEED = 18f;
  public static final float STANDARD_ARROW_RANGE = 50f;

  public static final float ICE_ARROW_SPEED = 16f;
  public static final float ICE_ARROW_RANGE = 50f;

  public static final float FIRE_ARROW_SPEED = 18f;
  public static final float FIRE_ARROW_RANGE = 50f;

  public static final float GRAPPLE_ARROW_SPEED = 22f;
  public static final float GRAPPLE_ARROW_RANGE = 50f;
  public static final float PLAYER_ARROW_WIDTH = 0.6f;
  public static final float PLAYER_ARROW_HEIGHT = 0.3f;

  public static final float POISON_POTION_SPEED = 12f;
  public static final float POISON_POTION_RANGE = 20f;
  public static final float POISON_POTION_WIDTH = 0.45f;
  public static final float POISON_POTION_HEIGHT = 0.55f;

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
    return createPlayerArrow(shooter, position, direction, 1f);
  }

  public static Entity createPlayerArrow(
      Entity shooter, Vector2 position, Vector2 direction, float speedMultiplier) {
    return createArrow(
        shooter,
        position,
        direction,
        ItemType.STANDARD_ARROW.getDamage(),
        STANDARD_ARROW_SPEED * speedMultiplier,
        STANDARD_ARROW_RANGE,
        ArrowType.STANDARD);
  }

  public static Entity createIceArrow(Entity shooter, Vector2 position, Vector2 direction) {
    return createIceArrow(shooter, position, direction, 1f);
  }

  public static Entity createIceArrow(
      Entity shooter, Vector2 position, Vector2 direction, float speedMultiplier) {
    return createArrow(
        shooter,
        position,
        direction,
        ItemType.ICE_ARROW.getDamage(),
        ICE_ARROW_SPEED * speedMultiplier,
        ICE_ARROW_RANGE,
        ArrowType.ICE);
  }

  public static Entity createFireArrow(Entity shooter, Vector2 position, Vector2 direction) {
    return createFireArrow(shooter, position, direction, 1f);
  }

  public static Entity createFireArrow(
      Entity shooter, Vector2 position, Vector2 direction, float speedMultiplier) {
    return createArrow(
        shooter,
        position,
        direction,
        ItemType.FIRE_ARROW.getDamage(),
        FIRE_ARROW_SPEED * speedMultiplier,
        FIRE_ARROW_RANGE,
        ArrowType.FIRE);
  }

  public static Entity createGrappleArrow(Entity shooter, Vector2 position, Vector2 direction) {
    return createGrappleArrow(shooter, position, direction, 1f);
  }

  /**
   * The grapple arrow never goes through the player's charge-release path, so {@code
   * speedMultiplier} is unused. This overload exists so {@code BowComponent} can type-check against
   * {@code ProjectileCreator}.
   */
  public static Entity createGrappleArrow(
      Entity shooter, Vector2 position, Vector2 direction, float speedMultiplier) {
    return createArrow(
        shooter,
        position,
        direction,
        ItemType.ROPE_ARROW.getDamage(),
        GRAPPLE_ARROW_SPEED,
        GRAPPLE_ARROW_RANGE,
        ArrowType.GRAPPLE);
  }

  public static Entity createPoisonArrow(Entity shooter, Vector2 position, Vector2 direction) {
    return createPoisonArrow(shooter, position, direction, 1f);
  }

  public static Entity createPoisonArrow(
      Entity shooter, Vector2 position, Vector2 direction, float speedMultiplier) {
    return createArrow(
        shooter,
        position,
        direction,
        ItemType.STANDARD_ARROW.getDamage(),
        STANDARD_ARROW_SPEED * speedMultiplier,
        STANDARD_ARROW_RANGE,
        ArrowType.POISON);
  }

  /**
   * Creates a thrown poison flask that applies the potion's poison debuff on impact.
   *
   * @param shooter entity that threw the flask
   * @param position world spawn position
   * @param direction throw direction
   * @return potion projectile
   */
  public static Entity createThrownPoisonPotion(
      Entity shooter, Vector2 position, Vector2 direction) {
    Entity potion =
        createArrow(
            shooter,
            position,
            direction,
            ItemType.PoisonPotion.getDamage(),
            POISON_POTION_SPEED,
            POISON_POTION_RANGE,
            ArrowType.POTION,
            ItemType.PoisonPotion.getPoisonDamagePerSecond(),
            ItemType.PoisonPotion.getPoisonDuration());
    potion.setScale(POISON_POTION_WIDTH, POISON_POTION_HEIGHT);
    return potion;
  }

  private static Entity createArrow(
      Entity shooter,
      Vector2 position,
      Vector2 direction,
      int damage,
      float speed,
      float range,
      ArrowType arrowType) {
    return createArrow(shooter, position, direction, damage, speed, range, arrowType, 0f, 0f);
  }

  private static Entity createArrow(
      Entity shooter,
      Vector2 position,
      Vector2 direction,
      int damage,
      float speed,
      float range,
      ArrowType arrowType,
      float poisonDamagePerSecond,
      float poisonDuration) {
    Vector2 normalizedDir = direction.cpy().nor();

    Entity arrow =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.DynamicBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER_PROJECTILE))
            .addComponent(new CombatStatsComponent(1, damage))
            .addComponent(
                new ArrowProjectileComponent(
                    shooter,
                    normalizedDir,
                    speed,
                    range,
                    arrowType,
                    poisonDamagePerSecond,
                    poisonDuration))
            .addComponent(new ArrowRenderComponent(arrowType));

    arrow.setScale(PLAYER_ARROW_WIDTH, PLAYER_ARROW_HEIGHT);
    arrow.setPosition(position.x - arrow.getScale().x / 2f, position.y - arrow.getScale().y / 2f);

    return arrow;
  }

  private ProjectileFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
