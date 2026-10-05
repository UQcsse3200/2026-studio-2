package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.TaskRunner;
import com.csse3200.game.entities.Entity;

/** Repositions an enemy a distance away from target */
public class FlyingRepositionTask extends DefaultTask implements PriorityTask {
  private final Entity target;
  private final int priority;
  private final float repositionDistance;

  private MovementTask movementTask;
  private boolean repositionRequired;

  public FlyingRepositionTask(Entity target, int priority, float repositionDistance) {
    this.target = target;
    this.priority = priority;
    this.repositionDistance = repositionDistance;
  }

  @Override
  public void create(TaskRunner taskRunner) {
    super.create(taskRunner);
    owner.getEntity().getEvents().addListener("hitPlayer", this::requestReposition);
  }

  public void requestReposition() {
    repositionRequired = true;
  }

  @Override
  public void start() {
    super.start();

    Vector2 enemyPos = owner.getEntity().getPosition();
    Vector2 targetPos = target.getPosition();
    Vector2 repositionDir = enemyPos.cpy().sub(targetPos).nor();
    Vector2 reposition = enemyPos.cpy().add(repositionDir.scl(repositionDistance));

    movementTask = new MovementTask(reposition);
    movementTask.create(owner);
    movementTask.start();

    repositionRequired = false;
  }

  @Override
  public void update() {
    movementTask.update();

    if (movementTask.getStatus() != Status.ACTIVE) {
      status = Status.FINISHED;
      repositionRequired = false;
    }
  }

  @Override
  public void stop() {
    super.stop();

    if (movementTask != null) {
      movementTask.stop();
    }
  }

  @Override
  public int getPriority() {
    if (status == Status.ACTIVE) {
      return getActivePriority();
    }

    if (repositionRequired) {
      return getActivePriority();
    }

    return getInactivePriority();
  }

  private int getActivePriority() {
    return priority;
  }

  private int getInactivePriority() {
    return -1;
  }
}
