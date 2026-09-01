package com.csse3200.game.components.tasks;

import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public class DelayedAttackTask extends DefaultTask implements PriorityTask {
  private final Entity target;
  private final int priority;
  private final float attackRange;
  private final float attackDelay;

  private float attackStartTime;

  public DelayedAttackTask(Entity target, int priority, float attackRange, float attackDelay) {
    this.target = target;
    this.priority = priority;
    this.attackRange = attackRange;
    this.attackDelay = attackDelay;
  }

  @Override
  public void start() {
    super.start();
    this.attackStartTime = ServiceLocator.getTimeSource().getTime();
  }

  @Override
  public void update() {
    long currentTime = ServiceLocator.getTimeSource().getTime();

    if (currentTime - attackDelay * 1000 >= attackStartTime) {

      float distance = owner.getEntity().getPosition().dst(target.getPosition());
      if (distance < attackRange) {
        target
            .getComponent(CombatStatsComponent.class)
            .hit(this.owner.getEntity().getComponent(CombatStatsComponent.class));
        attackStartTime = currentTime;
      }
    }
  }

  @Override
  public int getPriority() {
    float distance = owner.getEntity().getPosition().dst(target.getPosition());
    long currentTime = ServiceLocator.getTimeSource().getTime();

    if (distance < attackRange || currentTime - attackDelay * 1000 < attackStartTime) {
      return priority;
    } else {
      return -1;
    }
  }
}
