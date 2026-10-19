package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.ProjectileAttackComponent;
import com.csse3200.game.components.ProjectileComponent;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowTrajectory;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.item.ArrowRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/** Factory for creating enemy projectiles. The player's are made by {@link ProjectileFactory}. */
public class EnemyProjectileFactory {

  /**
   * Creates an arrow for a skeleton archer to fire at a target.
   *
   * <p>It is the player's arrow in every way that matters to how it flies: the same sprite, size
   * and hit area, the same arc under gravity, and the same handling of terrain, so it breaks on the
   * floor and walls just as the player's does. It is aimed so its arc comes down on the target. The
   * only differences are that it hurts the player and flies straight through other enemies.
   *
   * @param shooter the archer firing it, which the arrow never hits
   * @param spawnCenter where the middle of the arrow starts
   * @param targetCenter point the arrow is aimed to land on
   * @param damage damage dealt when the arrow hits the player
   * @param speed launch speed in world units per second
   * @param lifetime maximum arrow lifetime in seconds
   * @return arrow entity, already positioned at spawnCenter
   */
  public static Entity createSkeletonArcherProjectile(
      Entity shooter,
      Vector2 spawnCenter,
      Vector2 targetCenter,
      int damage,
      float speed,
      float lifetime) {

    float arcGravityY = PhysicsEngine.GRAVITY_Y * ArrowProjectileComponent.ARC_GRAVITY_SCALE;
    Vector2 aim = ArrowTrajectory.launchDirection(spawnCenter, targetCenter, speed, arcGravityY);

    Entity projectile =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.DynamicBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.ENEMY_PROJECTILE))
            .addComponent(new CombatStatsComponent(1, damage))
            .addComponent(
                new ArrowProjectileComponent(
                        shooter,
                        aim,
                        speed,
                        ProjectileFactory.STANDARD_ARROW_RANGE,
                        ArrowType.STANDARD)
                    .hurtsPlayer())
            .addComponent(new ProjectileComponent(lifetime))
            .addComponent(new ArrowRenderComponent(ArrowType.STANDARD));

    projectile.setScale(
        ProjectileFactory.PLAYER_ARROW_WIDTH, ProjectileFactory.PLAYER_ARROW_HEIGHT);
    projectile.setPosition(
        spawnCenter.x - projectile.getScale().x / 2f, spawnCenter.y - projectile.getScale().y / 2f);

    return projectile;
  }

  public static Entity createNecromancerProjectile(
      Vector2 targetPosition, int damage, float speed, float lifetime) {

    PhysicsMovementComponent movement = new PhysicsMovementComponent(new Vector2(speed, speed));
    movement.setTarget(targetPosition);

    Entity projectile =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(movement)
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.ENEMY_PROJECTILE))
            .addComponent(new CombatStatsComponent(1, damage))
            .addComponent(new ProjectileAttackComponent(PhysicsLayer.PLAYER))
            .addComponent(new ProjectileComponent(lifetime))
            .addComponent(new ColliderComponent())
            .addComponent(
                new TextureRenderComponent("images/projectiles/necromancer_projectile.png"))
            .addComponent(
                new PointLightComponent(
                    ServiceLocator.getLightingService().getEngine().getRayHandler(),
                    LightingDefaults.RAYS,
                    new Color(0.4f, 0f, 0.65f, 1f),
                    LightingDefaults.DIST));
    ;

    projectile.getComponent(TextureRenderComponent.class).scaleEntity();
    projectile.setScale(projectile.getScale().scl(0.8f));
    PhysicsUtils.setScaledCollider(projectile, 0.3f, 0.3f);

    return projectile;
  }

  /**
   * Creates a projectile used by Calypso during the first phase of the boss fight.
   *
   * @param targetPosition position the projectile travels towards
   * @param damage damage dealt when the projectile hits the player
   * @param speed projectile movement speed
   * @param lifetime maximum projectile lifetime in seconds
   * @return Calypso projectile entity
   */
  public static Entity createCalypsoProjectile(
      Vector2 targetPosition, int damage, float speed, float lifetime) {

    PhysicsMovementComponent movement = new PhysicsMovementComponent(new Vector2(speed, speed));
    movement.setTarget(targetPosition);

    Entity projectile =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(movement)
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
            .addComponent(new CombatStatsComponent(1, damage))
            .addComponent(new ProjectileAttackComponent(PhysicsLayer.PLAYER))
            .addComponent(new ProjectileComponent(lifetime))
            .addComponent(new ColliderComponent())
            // Temporary asset until the Calypso projectile sprite is available.
            .addComponent(
                new TextureRenderComponent("images/projectiles/necromancer_projectile.png"))
            .addComponent(
                new PointLightComponent(
                    ServiceLocator.getLightingService().getEngine().getRayHandler(),
                    LightingDefaults.RAYS,
                    new Color(0.4f, 0f, 0.65f, 1f),
                    LightingDefaults.DIST));
    ;

    projectile.getComponent(TextureRenderComponent.class).scaleEntity();
    projectile.setScale(projectile.getScale().scl(0.8f));
    PhysicsUtils.setScaledCollider(projectile, 0.3f, 0.3f);

    return projectile;
  }

  private EnemyProjectileFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
