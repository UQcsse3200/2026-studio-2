package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.AttackFactory;
import com.csse3200.game.services.ServiceLocator;

public class DelayedAttackTask extends DefaultTask implements PriorityTask {
  private final Entity target;
  private final int priority;
  private final float attackRange;
  private final float attackDelay;

  private float attackStartTime;
  private Entity attack;
  private boolean isAttacking = false;
  private float attackDuration = 1;

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

    // start attack after delay
    if (!isAttacking && currentTime >= attackStartTime + attackDelay * 1000) {
      createAttack();
      isAttacking = true;
    }

    // end attack after duration
    if (isAttacking
        && currentTime >= attackStartTime + attackDelay * 1000 + attackDuration * 1000) {
      stop();
    }
  }

  private void createAttack() {
    Vector2 size = new Vector2(attackRange * 2, 1);
    Vector2 position = owner.getEntity().getCenterPosition();

    attack = AttackFactory.createNewAttack(size, position, 1, 1f);
    ServiceLocator.getEntityService().register(attack);
  }

  @Override
  public void stop() {
    super.stop();
    isAttacking = false;
  }

  @Override
  public int getPriority() {
    float distance = owner.getEntity().getPosition().dst(target.getPosition());

    if (distance < attackRange || isAttacking) {
      return priority;
    } else {
      return -1;
    }
  }
}
