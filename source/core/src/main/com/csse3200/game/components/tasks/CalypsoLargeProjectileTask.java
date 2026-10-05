package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFact;
import com.csse3200.game.services.ServiceLocator;

/** Special ranged attack used by Calypso to periodically fire a large projectile at the target. */
public class CalypsoLargeProjectileTask extends DefaultTask implements PriorityTask {
  private final Entity target;
  private final int priority;
  private final float attackRange;
  private final float cooldown;
  private final int damage;
  private final float projectileSpeed;
  private final float projectileLifetime;

  private long lastAttackTime;

  /**
   * Creates Calypso's large projectile attack.
   *
   * @param target target entity to attack
   * @param priority priority when the attack is ready
   * @param attackRange maximum distance at which Calypso can attack
   * @param cooldown seconds between large projectile attacks
   * @param damage projectile damage
   * @param projectileSpeed projectile movement speed
   * @param projectileLifetime maximum projectile lifetime in seconds
   */
  public CalypsoLargeProjectileTask(
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
    this.lastAttackTime = ServiceLocator.getTimeSource().getTime();
  }

  @Override
  public void update() {
    fireProjectile();
    lastAttackTime = ServiceLocator.getTimeSource().getTime();
  }

  @Override
  public int getPriority() {
    float distance = owner.getEntity().getPosition().dst(target.getPosition());
    long currentTime = ServiceLocator.getTimeSource().getTime();

    boolean inRange = distance <= attackRange;
    boolean cooldownReady = currentTime - lastAttackTime >= cooldown * 1000;

    if (inRange && cooldownReady) {
      return priority;
    }

    return -1;
  }

  private void fireProjectile() {
    Entity calypso = owner.getEntity();

    Vector2 calypsoCenter = calypso.getCenterPosition();
    Vector2 targetCenter = target.getCenterPosition();

    float facingDirection = targetCenter.x >= calypsoCenter.x ? 1f : -1f;
    Vector2 spawnCenter = calypsoCenter.cpy().add(0.8f * facingDirection, -0.15f);

    Entity projectile =
        ProjectileFact.createCalypsoLargeProjectile(
            targetCenter, damage, projectileSpeed, projectileLifetime);

    Vector2 projectilePosition = spawnCenter.cpy().sub(projectile.getScale().cpy().scl(0.5f));
    projectile.setPosition(projectilePosition);

    ServiceLocator.getEntityService().register(projectile);
  }
}
