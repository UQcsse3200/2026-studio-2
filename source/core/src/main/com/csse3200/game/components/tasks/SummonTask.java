package com.csse3200.game.components.tasks;

import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.services.ServiceLocator;

/** AI task that allows an enemy to summon an enemy when the player is in range */
public class SummonTask extends DefaultTask implements PriorityTask {
  private final Entity target;
  private final int priority;
  private final float attackRange;
  private final float cooldown;

  private long lastSummonTime;

  /**
   * Creates a summon attack task
   *
   * @param target target entity to perform the summoning
   * @param priority task priority while target is in range
   * @param attackRange maximum distance at which the enemy can fire
   * @param cooldown seconds between attacks
   */
  public SummonTask(Entity target, int priority, float attackRange, float cooldown) {
    this.target = target;
    this.priority = priority;
    this.attackRange = attackRange;
    this.cooldown = cooldown;
  }

  @Override
  public void start() {
    super.start();
  }

  @Override
  public void update() {
    long currentTime = ServiceLocator.getTimeSource().getTime();

    if (currentTime - lastSummonTime >= cooldown * 1000) {
      summonSkeleton();
      lastSummonTime = currentTime;
    }
  }

  @Override
  public int getPriority() {
    float distance = owner.getEntity().getPosition().dst(target.getPosition());

    long currentTime = ServiceLocator.getTimeSource().getTime();

    boolean canSummon = currentTime - lastSummonTime >= cooldown * 1000;

    if (canSummon) {
      return priority;
    }

    return -1;
  }

  private void summonSkeleton() {
    Entity necromancer = owner.getEntity();

    Entity skeletonWarrior = EnemyFactory.createSkeletonWarrior(target);

    skeletonWarrior.setPosition(necromancer.getPosition().x + 1f, necromancer.getPosition().y);

    ServiceLocator.getEntityService().register(skeletonWarrior);
  }
}
