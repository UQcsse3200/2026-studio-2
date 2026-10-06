package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFact;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * AI task that allows an enemy to fire projectiles at a target while the target is within range.
 */
public class RangedAttackTask extends DefaultTask implements PriorityTask {
  private final Entity target;
  private final int priority;
  private final float attackRange;
  private final float cooldown;
  private final int damage;
  private final float projectileSpeed;
  private final float projectileLifetime;
  private final String attackType;

  private long lastAttackTime;
  private long currentTime;

  /**
   * Creates a ranged attack task.
   *
   * @param target target entity to attack
   * @param priority task priority while target is in range
   * @param attackRange maximum distance at which the enemy can fire
   * @param cooldown seconds between attacks
   * @param damage projectile damage
   * @param projectileSpeed projectile movement speed
   * @param projectileLifetime maximum projectile lifetime in seconds
   * @param useNecromancerProjectile whether to use the necromancer projectile
   * @param useCalypsoProjectile whether to use the Calypso projectile
   */
  public RangedAttackTask(
      Entity target,
      int priority,
      float attackRange,
      float cooldown,
      int damage,
      float projectileSpeed,
      float projectileLifetime,
      String attackType) {
    this.target = target;
    this.priority = priority;
    this.attackRange = attackRange;
    this.cooldown = cooldown;
    this.damage = damage;
    this.projectileSpeed = projectileSpeed;
    this.projectileLifetime = projectileLifetime;
    this.attackType = attackType;
  }

  @Override
  public void start() {
    super.start();

    if (lastAttackTime == 0) {
      lastAttackTime = ServiceLocator.getTimeSource().getTime();
    }
  }

  @Override
  public void update() {
    fireProjectile();
    lastAttackTime = ServiceLocator.getTimeSource().getTime();
  }

  @Override
  public int getPriority() {
    float distance = owner.getEntity().getPosition().dst(target.getPosition());
    currentTime = ServiceLocator.getTimeSource().getTime();

    if (distance <= attackRange && currentTime - lastAttackTime >= cooldown * 1000) {
      return priority;
    }

    return -1;
  }

  private void fireProjectile() {
    Entity enemy = owner.getEntity();

    Vector2 enemyCenter = enemy.getCenterPosition();
    Vector2 targetCenter = target.getCenterPosition();

    // Spawn slightly towards the player and slightly below the enemy centre.
    float facingDirection = targetCenter.x >= enemyCenter.x ? 1f : -1f;

    Vector2 spawnCenter = enemyCenter.cpy().add(0.8f * facingDirection, -0.15f);

    Entity projectile;

    if (attackType.equals("calypso")) {
      projectile =
          ProjectileFact.createCalypsoProjectile(
              targetCenter, damage, projectileSpeed, projectileLifetime);
    } else if (attackType.equals("summon")) {
      projectile =
          ProjectileFact.createNecromancerProjectile(
              targetCenter, damage, projectileSpeed, projectileLifetime);
    } else {
      projectile =
          ProjectileFact.createSkeletonArcherProjectile(
              targetCenter, damage, projectileSpeed, projectileLifetime);
    }
    // setPosition() uses the bottom-left corner, so offset by half the
    // projectile size to place its centre at spawnCenter.
    Vector2 projectilePosition = spawnCenter.cpy().sub(projectile.getScale().cpy().scl(0.5f));

    projectile.setPosition(projectilePosition);

    // Rotate the arrow towards the player.
    Vector2 direction = targetCenter.cpy().sub(spawnCenter);
    float angle = direction.angleDeg();

    projectile.getComponent(TextureRenderComponent.class).setRotation(angle);

    ServiceLocator.getEntityService().register(projectile);
  }
}
