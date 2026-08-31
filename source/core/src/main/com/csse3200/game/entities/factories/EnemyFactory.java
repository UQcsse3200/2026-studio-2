package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.EnemyDeathComponent;
import com.csse3200.game.components.npc.GhostAnimationController;
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
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class EnemyFactory {
  private static final EnemyConfigs configs =
      FileLoader.readClass(EnemyConfigs.class, "configs/Enemies.json");

  // Test function for checking enemy behaviour
  public static Entity createChaser(Entity target) {
    EnemyConfig config = configs.chaser;
    Entity chaser = createEnemy(target, config);

    chaser.addComponent(new TextureRenderComponent("images/skeleton_warrior.png"));
    chaser.getComponent(AnimationRenderComponent.class).scaleEntity();

    chaser
        .getComponent(AITaskComponent.class)
        .addTask(new DelayedAttackTask(target, 20, 0.8f, 0.5f));

    return chaser;
  }

  // Test function for checking enemy behaviour that shoots
  public static Entity createShooter(Entity target) {
    EnemyConfig config = configs.shooter;
    Entity shooter = createEnemy(target, config);

    shooter
        // .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
        .addComponent(new TextureRenderComponent("images/skeleton_archer.png"));

    shooter.getComponent(TextureRenderComponent.class).scaleEntity();

    return shooter;
  }

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
