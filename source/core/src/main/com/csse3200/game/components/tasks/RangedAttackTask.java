package com.csse3200.game.components.tasks;

import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
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

  private long lastAttackTime;

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
   */
  public RangedAttackTask(
      Entity target,
      int priority,
      float attackRange,
      float cooldown,
      int damage,
      float projectileSpeed,
      float projectileLifetime) {
    this.target = target;
    this.priority = priority;
    this.attackRange = attackRange;
    this.cooldown = cooldown;
    this.damage = damage;
    this.projectileSpeed = projectileSpeed;
    this.projectileLifetime = projectileLifetime;
  }

  @Override
  public void start() {
    super.start();
    lastAttackTime = 0;
  }

  @Override
  public void update() {
    long currentTime = ServiceLocator.getTimeSource().getTime();

    if (currentTime - lastAttackTime >= cooldown * 1000) {
      fireProjectile();
      lastAttackTime = currentTime;
    }
  }

  @Override
  public int getPriority() {
    float distance = owner.getEntity().getPosition().dst(target.getPosition());

    if (distance <= attackRange) {
      return priority;
    }

    return -1;
  }

  private void fireProjectile() {
    Entity enemy = owner.getEntity();

    Entity projectile =
        ProjectileFactory.createEnemyProjectile(
            target.getPosition(), damage, projectileSpeed, projectileLifetime);

    projectile.setPosition(enemy.getCenterPosition());
    ServiceLocator.getEntityService().register(projectile);
  }
}