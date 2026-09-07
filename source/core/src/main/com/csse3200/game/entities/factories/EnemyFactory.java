package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
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
    Entity skeletonWarrior = createEnemy(target, config);

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
    Entity SkeletonArcher = createEnemy(target, config);

    SkeletonArcher
        // .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
        .addComponent(new TextureRenderComponent("images/skeleton_archer.png"));

    SkeletonArcher.getComponent(TextureRenderComponent.class).scaleEntity();

    return SkeletonArcher;
  }

  /**
   * Creates a base enemy entity
   *
   * @param target entity the enemy will chase
   * @param config stats and behaviour values loaded from Enemies.json
   * @return base enemy entity, without a render component
   */
  public static Entity createEnemy(Entity target, EnemyConfig config) {
    AITaskComponent aiComponent =
        new AITaskComponent()
            .addTask(
                // Adding the values for wander task from the enemy's config file
                new WanderTask(
                    new Vector2(config.wanderRangeX, config.wanderRangeY), config.wanderWaitTime))
            .addTask(
                // Adding the values for chase task from the enemy's config file
                new ChaseTask(
                    target, config.chasePriority, config.viewDistance, config.maxChaseDistance));

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

  private EnemyFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
