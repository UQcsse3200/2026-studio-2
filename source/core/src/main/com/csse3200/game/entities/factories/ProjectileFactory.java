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

  /**
   * @return the speed an arrow of this type is launched at before any charge multiplier is applied
   */
  public static float getArrowSpeed(ArrowType type) {
    return switch (type) {
      case ICE -> ICE_ARROW_SPEED;
      case FIRE -> FIRE_ARROW_SPEED;
      case GRAPPLE -> GRAPPLE_ARROW_SPEED;
      case POTION -> POISON_POTION_SPEED;
      case STANDARD, POISON -> STANDARD_ARROW_SPEED;
    };
  }

  /**
   * @return how far, in a straight line from the shooter, an arrow of this type flies before
   *     expiring
   */
  public static float getArrowRange(ArrowType type) {
    return switch (type) {
      case ICE -> ICE_ARROW_RANGE;
      case FIRE -> FIRE_ARROW_RANGE;
      case GRAPPLE -> GRAPPLE_ARROW_RANGE;
      case POTION -> POISON_POTION_RANGE;
      case STANDARD, POISON -> STANDARD_ARROW_RANGE;
    };
  }

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
   * Like every other arrow, a charged shot is just launched faster or slower - how far it gets then
   * falls out of the arc, so the range cap stays the same however hard it was drawn.
   */
  public static Entity createGrappleArrow(
      Entity shooter, Vector2 position, Vector2 direction, float speedMultiplier) {
    return createArrow(
        shooter,
        position,
        direction,
        ItemType.ROPE_ARROW.getDamage(),
        GRAPPLE_ARROW_SPEED * speedMultiplier,
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
            position,
            ItemType.PoisonPotion.getDamage(),
            new ArrowProjectileComponent(
                shooter,
                direction.cpy().nor(),
                POISON_POTION_SPEED,
                POISON_POTION_RANGE,
                ArrowType.POTION,
                ItemType.PoisonPotion.getPoisonDamagePerSecond(),
                ItemType.PoisonPotion.getPoisonDuration()),
            ArrowType.POTION);
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
    return createArrow(
        position,
        damage,
        new ArrowProjectileComponent(shooter, direction.cpy().nor(), speed, range, arrowType),
        arrowType);
  }

  private static Entity createArrow(
      Vector2 position, int damage, ArrowProjectileComponent projectile, ArrowType arrowType) {
    Entity arrow =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.DynamicBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER_PROJECTILE))
            .addComponent(new CombatStatsComponent(1, damage))
            .addComponent(projectile)
            .addComponent(new ArrowRenderComponent(arrowType));

    arrow.setScale(PLAYER_ARROW_WIDTH, PLAYER_ARROW_HEIGHT);
    arrow.setPosition(position.x - arrow.getScale().x / 2f, position.y - arrow.getScale().y / 2f);

    return arrow;
  }

  private ProjectileFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
