package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.BurnStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.EnemyDeathComponent;
import com.csse3200.game.components.EnemyItemDropComponent;
import com.csse3200.game.components.EnemyTeleportComponent;
import com.csse3200.game.components.PoisonStatsComponent;
import com.csse3200.game.components.SlowStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.npc.EnemyAnimationController;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.components.tasks.DelayedAttackTask;
import com.csse3200.game.components.tasks.FlyingChaseTask;
import com.csse3200.game.components.tasks.FlyingRepositionTask;
import com.csse3200.game.components.tasks.RangedAttackTask;
import com.csse3200.game.components.tasks.RepositionTask;
import com.csse3200.game.components.tasks.SummonTask;
import com.csse3200.game.components.tasks.SweepAttackTask;
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
import com.csse3200.game.rendering.EnemyHealthRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;

/**
 * Factory to create enemy entities.
 *
 * <p>Each enemy type has a creation method that returns an entity. Stats and behaviour values are
 * loaded from {@code configs/Enemies.json} and mapped to {@link EnemyConfigs}
 */
public class EnemyFactory {
  private static final EnemyConfigs configs =
      FileLoader.readClass(EnemyConfigs.class, "configs/Enemies.json");

  private static final float CALYPSO_SWEEP_DURATION = 0.8f;
  private static final float CALYPSO_IDLE_FRAME_TIME = 0.15f;
  private static final float CALYPSO_WALK_FRAME_TIME = 0.15f;
  private static final float CALYPSO_WIDTH = 1f;
  private static final int CALYPSO_SWEEP_HIT_FRAME = 2;
  private static final int CALYPSO_SWEEP_PRIORITY = 22;
  private static final float CALYPSO_SWEEP_RANGE = 1.0f;
  private static final float CALYPSO_SWEEP_HEIGHT = 1.5f;
  private static final float CALYPSO_SWEEP_COOLDOWN = 1.5f;
  private static final float CALYPSO_SWEEP_ANCHOR_X = 218.5f;
  private static final float CALYPSO_SWEEP_ANCHOR_Y = 4f;

  /**
   * Creates a melee skeleton warrior that chases and attacks the target after a delay.
   *
   * @param target entity the enemy will chase and attack
   * @return skeleton warrior entity
   */
  public static Entity createSkeletonWarrior(Entity target) {
    EnemyConfig config = configs.skeletonWarrior;
    Entity skeletonWarrior = createEnemy(target, config);

    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            ServiceLocator.getResourceService()
                .getAsset("images/skeleton_warrior.atlas", TextureAtlas.class));
    animator.addAnimation("walk", 0.15f, Animation.PlayMode.LOOP);
    animator.addAnimation("idle", 0.15f, Animation.PlayMode.LOOP);

    skeletonWarrior.addComponent(new EnemyAnimationController(target));
    skeletonWarrior.addComponent(animator);

    // Skeleton Warrior has a charged attack (extra range melee with initial delay)
    skeletonWarrior
        .getComponent(AITaskComponent.class)
        .addTask(new DelayedAttackTask(target, 20, config.attackRange, 0.5f));

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

    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            ServiceLocator.getResourceService()
                .getAsset("images/skeleton_archer.atlas", TextureAtlas.class));
    animator.addAnimation("walk", 0.15f, Animation.PlayMode.LOOP);
    animator.addAnimation("idle", 0.15f, Animation.PlayMode.LOOP);

    SkeletonArcher
        // .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
        .addComponent(animator)
        .addComponent(new EnemyAnimationController(target));

    SkeletonArcher.getComponent(AnimationRenderComponent.class).scaleEntity();

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
   * Creates a flying vulture that attack the player from the sky
   *
   * @param target entity the enemy will chase and shoot at
   * @return skeleton archer entity
   */
  public static Entity createVulture(Entity target) {
    EnemyConfig config = configs.vulture;
    Entity Vulture = createEnemy(target, config);

    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            ServiceLocator.getResourceService()
                .getAsset("images/vulture.atlas", TextureAtlas.class));
    animator.addAnimation("walk", 0.15f, Animation.PlayMode.LOOP);
    animator.addAnimation("idle", 0.15f, Animation.PlayMode.LOOP);

    Vulture
        // .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
        .addComponent(animator)
        .addComponent(new EnemyAnimationController(target));

    Vulture.getComponent(AnimationRenderComponent.class).scaleEntity();
    // Vulture.getComponent(ColliderComponent.class).setSensor(true);

    return Vulture;
  }

  /**
   * Creates a necromancer that summons skeleton warriors and fire magic projectile
   *
   * @param target entity the enemy will chase and shoot at
   * @return skeleton archer entity
   */
  public static Entity createNecromancer(Entity target) {
    EnemyConfig config = configs.necromancer;
    Entity Necromancer = createEnemy(target, config);

    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            ServiceLocator.getResourceService()
                .getAsset("images/necromancer.atlas", TextureAtlas.class));
    animator.addAnimation("walk", 0.15f, Animation.PlayMode.LOOP);
    animator.addAnimation("idle", 0.15f, Animation.PlayMode.LOOP);

    Necromancer
        // .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
        .addComponent(animator)
        .addComponent(new EnemyAnimationController(target));

    Necromancer.getComponent(AnimationRenderComponent.class).scaleEntity();

    return Necromancer;
  }

  /**
   * Creates a cyclops miniboss
   *
   * @param target entity the enemy will chase and attack
   * @return cyclops entity
   */
  public static Entity createCyclops(Entity target) {
    EnemyConfig config = configs.cyclops;
    Entity cyclops = createEnemy(target, config);

    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            ServiceLocator.getResourceService()
                .getAsset("images/skeleton_warrior.atlas", TextureAtlas.class));
    animator.addAnimation("walk", 0.15f, Animation.PlayMode.LOOP);
    animator.addAnimation("idle", 0.15f, Animation.PlayMode.LOOP);

    cyclops.addComponent(new EnemyAnimationController(target));
    cyclops.addComponent(animator);

    cyclops
        .getComponent(AITaskComponent.class)
        .addTask(new DelayedAttackTask(target, 20, config.attackRange, 0.5f));

    return cyclops;
  }

  /**
   * Creates a calypso mainboss
   *
   * @param target entity the enemy will chase and attack
   * @return cyclops entity
   */
  public static Entity createCalypso(Entity target, List<Vector2> tpPositions) {
    EnemyConfig config = configs.calypso;
    Entity calypso = createEnemy(target, config);

    TextureAtlas atlas =
        ServiceLocator.getResourceService().getAsset("images/calypso.atlas", TextureAtlas.class);
    AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
    animator.addAnimation("idle", CALYPSO_IDLE_FRAME_TIME, Animation.PlayMode.LOOP);
    animator.addAnimation("walk", CALYPSO_WALK_FRAME_TIME, Animation.PlayMode.LOOP);

    int sweepFrames = atlas.findRegions("sweep").size;
    float sweepFrameTime = sweepFrames > 0 ? CALYPSO_SWEEP_DURATION / sweepFrames : 0f;
    animator.addAnimation(
        "sweep",
        sweepFrameTime,
        Animation.PlayMode.NORMAL,
        CALYPSO_SWEEP_ANCHOR_X,
        CALYPSO_SWEEP_ANCHOR_Y);
    float sweepWindUp =
        sweepFrames > 0
            ? Math.min(CALYPSO_SWEEP_HIT_FRAME, sweepFrames - 1) * sweepFrameTime
            : CALYPSO_SWEEP_DURATION / 2f;

    calypso.addComponent(new EnemyAnimationController(target));
    calypso.addComponent(animator);
    calypso.addComponent(new EnemyTeleportComponent(tpPositions, 500L, 10000L));

    animator.scaleEntity();
    float targetHeight = target.getScale().y;
    if (targetHeight > 0f) {
      calypso.scaleHeight(targetHeight);
    } else {
      calypso.scaleWidth(CALYPSO_WIDTH);
    }
    PhysicsUtils.setScaledCollider(calypso, 0.55f, 0.4f);
    float sweepRange = CALYPSO_SWEEP_RANGE * calypso.getScale().x;

    // Telegraphed melee sweep: triggers "attackStart" (plays the sweep animation) and "hitPlayer"
    // when it connects (EnemyTeleportComponent / RepositionTask react to that).
    calypso
        .getComponent(AITaskComponent.class)
        .addTask(
            new SweepAttackTask(
                target,
                CALYPSO_SWEEP_PRIORITY,
                sweepRange,
                CALYPSO_SWEEP_HEIGHT,
                sweepWindUp,
                CALYPSO_SWEEP_DURATION,
                CALYPSO_SWEEP_COOLDOWN));

    return calypso;
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

    AITaskComponent aiComponent = new AITaskComponent();

    Entity enemy =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(
                new PhysicsMovementComponent(
                    new Vector2(config.maxSpeed, config.maxSpeed), config.gravity))
            .addComponent(new ColliderComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
            .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
            .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 10f))
            .addComponent(new EnemyDeathComponent())
            .addComponent(new PoisonStatsComponent())
            .addComponent(new BurnStatsComponent())
            .addComponent(new SlowStatsComponent())
            .addComponent(new EnemyItemDropComponent(config.itemDrops))
            .addComponent(new EnemyHealthRenderComponent())
            .addComponent(aiComponent);

    PhysicsUtils.setScaledCollider(enemy, 0.9f, 0.4f); // 0.4f seems small: any reason?

    aiComponent.addTask(
        // Adding the values for wander task from the enemy's config file
        new WanderTask(
            new Vector2(config.wanderRangeX, config.wanderRangeY), config.wanderWaitTime));
    if (config.attackType.equals("calypso")) {
      aiComponent.addTask(
          new ChaseTask(target, config.chasePriority, viewDistance, maxChaseDistance));
    } else if (config.behaviour.equals("flying")) {
      aiComponent
          .addTask(
              new FlyingChaseTask(target, config.chasePriority, viewDistance, maxChaseDistance))
          .addTask(
              new FlyingRepositionTask(
                  target, config.repositionPriority, config.repositionDistance));
    } else {
      aiComponent
          .addTask(new ChaseTask(target, config.chasePriority, viewDistance, maxChaseDistance))
          .addTask(
              new RepositionTask(target, config.repositionPriority, config.repositionDistance));
    }

    // If the enemy is a range type, add a range task.
    if (config.attackType.equals("range")) {
      aiComponent.addTask(
          new RangedAttackTask(
              target, 20, config.attackRange, 2f, config.baseAttack, 4.5f, 5f, config.attackType));
      // If the enemy is a summon type, add summon + range task
    } else if (config.attackType.equals("summon")) {
      aiComponent
          .addTask(
              new RangedAttackTask(
                  target,
                  20,
                  config.attackRange,
                  10f,
                  config.baseAttack,
                  4.5f,
                  5f,
                  config.attackType))
          .addTask(new SummonTask(target, 30, config.attackRange, 5f));
    } else if (config.attackType.equals("cyclops")) {
      // add melee sweep attack and throwing boulder range attack
    } else if (config.attackType.equals("calypso")) {
      // Standard projectile attack. Other Calypso attacks are added separately.
      aiComponent.addTask(
          new RangedAttackTask(
              target, 20, 50f, 7f, config.baseAttack, 4.5f, 5f, config.attackType));
    }

    return enemy;
  }

  private static Entity createPassiveEnemy(EnemyConfig config) {
    return new Entity()
        .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
        .addComponent(new ColliderComponent())
        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
        .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
        .addComponent(new EnemyDeathComponent())
        .addComponent(new PoisonStatsComponent())
        .addComponent(new BurnStatsComponent())
        .addComponent(new SlowStatsComponent());
  }

  private EnemyFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
