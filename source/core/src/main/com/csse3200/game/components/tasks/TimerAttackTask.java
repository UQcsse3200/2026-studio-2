package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.BossPhaseComponent;
import com.csse3200.game.components.DurationComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class TimerAttackTask extends DelayedAttackTask {
  public static final String LASER_START_EVENT = "laserStart";
  public static final String LASER_END_EVENT = "laserEnd";

  public static final String BEAM_TEXTURE = "images/projectiles/calypso_laser_beam.png";
  private static final float BEAM_START_X_RATIO = 0.39f;
  private static final float BEAM_Y_OFFSET_RATIO = 0.16f;
  private static final float BEAM_THICKNESS = 1f;
  private final float attackInterval;
  private boolean windingUp;

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
  public void start() {
    super.start();
    windingUp = true;
    owner.getEntity().getEvents().trigger(LASER_START_EVENT);
  }

  @Override
  public void update() {
    boolean wasAttacking = isAttacking;
    super.update();
    if (!wasAttacking && isAttacking) {
      windingUp = false;
      spawnBeamVisual();
    }
    if (wasAttacking && !isAttacking) {
      owner.getEntity().getEvents().trigger(LASER_END_EVENT);
    }
  }

  @Override
  public void stop() {
    boolean active = windingUp || isAttacking;
    super.stop();
    windingUp = false;
    if (active) {
      owner.getEntity().getEvents().trigger(LASER_END_EVENT);
    }
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

    if (isAttacking || windingUp || (distance < attackRange && cooldownComplete)) {
      return priority;
    } else {
      return -1;
    }
  }

  private void spawnBeamVisual() {
    Entity enemy = owner.getEntity();
    Vector2 centre = enemy.getCenterPosition();
    float direction = target.getCenterPosition().x - centre.x < 0 ? -1f : 1f;

    float startX = centre.x + direction * BEAM_START_X_RATIO * enemy.getScale().x;
    float centreY = centre.y + BEAM_Y_OFFSET_RATIO * enemy.getScale().y;
    float length = attackRange;

    TextureRenderComponent render = new TextureRenderComponent(BEAM_TEXTURE);
    render.setRotation(direction > 0f ? 0f : 180f);

    Entity beam =
        new Entity().addComponent(render).addComponent(new DurationComponent(attackDuration));
    beam.setScale(length, BEAM_THICKNESS);
    beam.setPosition(direction > 0f ? startX : startX - length, centreY - BEAM_THICKNESS / 2f);
    ServiceLocator.getEntityService().register(beam);
  }
}
