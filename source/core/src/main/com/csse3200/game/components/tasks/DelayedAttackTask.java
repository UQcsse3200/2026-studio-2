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
  private final int attackDamage;
  private final float attackKnockback;
  // Can add attack Duration if necessary

  private float attackStartTime;
  private Entity attack;
  private boolean isAttacking = false;
  private float attackDuration = 1;

  public DelayedAttackTask(Entity target, int priority, float attackRange) {
    this(target, priority, attackRange, 0, 2, 0f);
  }

  public DelayedAttackTask(
      Entity target,
      int priority,
      float attackRange,
      float attackDelay,
      int attackDamage,
      float attackKnockback) {
    this.target = target;
    this.priority = priority;
    this.attackRange = attackRange;
    this.attackDelay = attackDelay;
    this.attackDamage = attackDamage;
    this.attackKnockback = attackKnockback;
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
      isAttacking = false;
      attackStartTime = currentTime;
    }
  }

  private void createAttack() {
    Vector2 size = new Vector2(attackRange, 1);

    Vector2 enemyPosition = owner.getEntity().getCenterPosition();
    float enemyWidth = owner.getEntity().getScale().x;
    float direction = target.getCenterPosition().sub(enemyPosition).x < 0 ? -1f : 1f;
    float offsetX = direction * (enemyWidth + size.x) / 2;

    Vector2 attackOffset = new Vector2(offsetX, 0);

    attack = AttackFactory.createNewAttack(size, attackOffset, attackDamage, 1f, attackKnockback);
    attack.setPosition(enemyPosition);
    ServiceLocator.getEntityService().register(attack);
  }

  @Override
  public void stop() {
    super.stop();
    isAttacking = false;
  }

  @Override
  public int getPriority() {
    float distance = owner.getEntity().getCenterPosition().dst(target.getCenterPosition());

    if (distance < attackRange || isAttacking) {
      return priority;
    } else {
      return -1;
    }
  }
}
