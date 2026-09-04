package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.EnemyDeathComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.components.tasks.DelayedAttackTask;
import com.csse3200.game.components.tasks.RangedAttackTask;
import com.csse3200.game.components.tasks.WanderTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EnemyConfig;
import com.csse3200.game.entities.configs.EnemyConfigs;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Factory to create enemy entities.
 *
 * <p>Each enemy type has a creation method that returns an entity. Stats and behaviour values are
 * loaded from {@code configs/Enemies.json} and mapped to {@link EnemyConfigs}
 */
public class EnemyFactory {
  private static final EnemyConfigs configs =
      FileLoader.readClass(EnemyConfigs.class, "configs/Enemies.json");

  /**
   * Creates a melee skeleton warrior that chases and attacks the target after a delay.
   *
   * @param target entity the enemy will chase and attack
   * @return skeleton warrior entity
   */
  public static Entity createSkeletonWarrior(Entity target) {
    EnemyConfig config = configs.skeletonWarrior;
    return createSkeletonWarrior(target, config.viewDistance, config.maxChaseDistance);
  }

  /**
   * Creates a melee skeleton warrior with custom chase distances.
   *
   * @param target entity the enemy will chase and attack
   * @param viewDistance distance at which chasing can begin
   * @param maxChaseDistance distance at which an active chase ends
   * @return skeleton warrior entity
   */
  public static Entity createSkeletonWarrior(
      Entity target, float viewDistance, float maxChaseDistance) {
    EnemyConfig config = configs.skeletonWarrior;
    Entity skeletonWarrior = createEnemy(target, config, viewDistance, maxChaseDistance);

    skeletonWarrior.addComponent(new TextureRenderComponent("images/skeleton_warrior.png"));
    skeletonWarrior.getComponent(TextureRenderComponent.class).scaleEntity();

    skeletonWarrior
        .getComponent(AITaskComponent.class)
        .addTask(new DelayedAttackTask(target, 20, 0.8f, 0.5f));

    return skeletonWarrior;
  }

  /**
   * Creates a ranged skeleton archer that fires projectiles at the target from a distance.
   *
   * @param target entity the enemy will chase and shoot at
   * @return skeleton archer entity
   */
  public static Entity createSkeletonArcher(Entity target) {
    EnemyConfig config = configs.skeletonArcher;
    return createSkeletonArcher(target, config.viewDistance, config.maxChaseDistance);
  }

  /**
   * Creates a ranged skeleton archer with custom chase distances.
   *
   * @param target entity the enemy will chase and attack
   * @param viewDistance distance at which chasing can begin
   * @param maxChaseDistance distance at which an active chase ends
   * @return skeleton archer entity
   */
  public static Entity createSkeletonArcher(
      Entity target, float viewDistance, float maxChaseDistance) {
    EnemyConfig config = configs.skeletonArcher;
    Entity SkeletonArcher = createEnemy(target, config, viewDistance, maxChaseDistance);

    SkeletonArcher
        // .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
        .addComponent(new TextureRenderComponent("images/skeleton_archer.png"));

    SkeletonArcher.getComponent(TextureRenderComponent.class).scaleEntity();

    return SkeletonArcher;
  }

  /**
   * Creates a stationary skeleton warrior for combat testing. The enemy can take damage and die,
   * but has no movement or attack AI.
   *
   * @return passive skeleton warrior entity
   */
  public static Entity createPassiveSkeletonWarrior() {
    Entity skeletonWarrior = createPassiveEnemy(configs.skeletonWarrior);
    skeletonWarrior.addComponent(new TextureRenderComponent("images/skeleton_warrior.png"));
    skeletonWarrior.getComponent(TextureRenderComponent.class).scaleEntity();
    PhysicsUtils.setScaledCollider(skeletonWarrior, 1.2f, 0.7f);
    return skeletonWarrior;
  }

  /**
   * Creates a stationary skeleton archer for combat testing. The enemy can take damage and die, but
   * has no movement or attack AI.
   *
   * @return passive skeleton archer entity
   */
  public static Entity createPassiveSkeletonArcher() {
    Entity skeletonArcher = createPassiveEnemy(configs.skeletonArcher);
    skeletonArcher.addComponent(new TextureRenderComponent("images/skeleton_archer.png"));
    skeletonArcher.getComponent(TextureRenderComponent.class).scaleEntity();
    PhysicsUtils.setScaledCollider(skeletonArcher, 1.2f, 0.7f);
    return skeletonArcher;
  }

  /**
   * Creates a base enemy entity
   *
   * @param target entity the enemy will chase
   * @param config stats and behaviour values loaded from Enemies.json
   * @return base enemy entity, without a render component
   */
  public static Entity createEnemy(Entity target, EnemyConfig config) {
    return createEnemy(target, config, config.viewDistance, config.maxChaseDistance);
  }

  private static Entity createEnemy(
      Entity target, EnemyConfig config, float viewDistance, float maxChaseDistance) {
    AITaskComponent aiComponent =
        new AITaskComponent()
            .addTask(
                // Adding the values for wander task from the enemy's config file
                new WanderTask(
                    new Vector2(config.wanderRangeX, config.wanderRangeY), config.wanderWaitTime))
            .addTask(
                // Adding the values for chase task from the enemy's config file
                new ChaseTask(target, config.chasePriority, viewDistance, maxChaseDistance));

    // If the enemy is a range type, add a range task.
    if (config.attackType.equals("range")) {
      aiComponent.addTask(
          new RangedAttackTask(target, 20, config.attackRange, 2f, config.baseAttack, 5f, 5f));
    }

    Entity enemy =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
            .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
            .addComponent(new EnemyDeathComponent())
            .addComponent(aiComponent);

    PhysicsUtils.setScaledCollider(enemy, 0.9f, 0.4f);

    return enemy;
  }

  private static Entity createPassiveEnemy(EnemyConfig config) {
    return new Entity()
        .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
        .addComponent(new ColliderComponent())
        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
        .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
        .addComponent(new EnemyDeathComponent());
  }

  private EnemyFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
