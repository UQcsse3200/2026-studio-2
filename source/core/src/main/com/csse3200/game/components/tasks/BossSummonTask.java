package com.csse3200.game.components.tasks;

import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.components.BossPhaseComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.services.ServiceLocator;

public class BossSummonTask extends DefaultTask implements PriorityTask {
  private final Entity target;
  private final int priority;
  private final float attackRange;
  private final float cooldown;

  private long lastSummonTime;

  public BossSummonTask(Entity target, int priority, float attackRange, float cooldown) {
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
    Entity calypso = owner.getEntity();

    BossPhaseComponent phase = calypso.getComponent(BossPhaseComponent.class);

    if (phase == null || phase.getCurrentPhase() < 2) {
      return -1;
    }

    float distance = calypso.getPosition().dst(target.getPosition());
    long currentTime = ServiceLocator.getTimeSource().getTime();

    if (distance <= attackRange && currentTime - lastSummonTime >= cooldown * 1000) {
      return priority;
    }

    return -1;
  }

  private void summonSkeleton() {
    Entity calypso = owner.getEntity();

    Entity skeleton = EnemyFactory.createSkeletonWarrior(target);
    skeleton.setPosition(calypso.getPosition().x + 1f, calypso.getPosition().y);

    calypso.getEvents().trigger("spawnEntity", skeleton);
  }
}
