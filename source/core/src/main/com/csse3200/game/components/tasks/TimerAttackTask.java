package com.csse3200.game.components.tasks;

import com.csse3200.game.components.BossPhaseComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public class TimerAttackTask extends DelayedAttackTask {
  private final float attackInterval;

  public TimerAttackTask(
      Entity target,
      int priority,
      float attackRange,
      float attackDelay,
      int attackDamage,
      float attackKnockback,
      float attackInterval) {
    super(target, priority, attackRange, attackDelay, attackDamage, attackKnockback);
    this.attackInterval = attackInterval;
  }

  @Override
  public int getPriority() {
    float distance = owner.getEntity().getCenterPosition().dst(target.getCenterPosition());
    long currentTime = ServiceLocator.getTimeSource().getTime();

    BossPhaseComponent phase = owner.getEntity().getComponent(BossPhaseComponent.class);
    if (phase.getCurrentPhase() < 2) {
      return -1;
    }

    boolean cooldownComplete = currentTime > attackStartTime + attackInterval * 1000;

    if (isAttacking || (distance < attackRange && cooldownComplete)) {
      return priority;
    } else {
      return -1;
    }
  }
}
