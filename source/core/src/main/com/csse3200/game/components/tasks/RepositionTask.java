package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.TaskRunner;
import com.csse3200.game.entities.Entity;

/** Repositions an enemy a distance away from target */
public class RepositionTask extends DefaultTask implements PriorityTask {
  private final Entity target;
  private final int priority;
  private final float repositionDistance;

  private MovementTask movementTask;
  private boolean repositionRequired;

  private EdgeDetectTask edgeDetector;

  public RepositionTask(Entity target, int priority, float repositionDistance) {
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
    Vector2 repositionDir = directionAwayFrom(enemyPos, targetPos);
    Vector2 reposition = enemyPos.cpy().add(repositionDir.scl(repositionDistance));

    movementTask = new MovementTask(reposition);
    movementTask.create(owner);
    movementTask.start();

    repositionRequired = false;
  }

  @Override
  public void update() {
    // check for edge
    Vector2 enemyPos = owner.getEntity().getPosition();
    Vector2 targetPos = target.getPosition();
    float direction = Math.signum(enemyPos.x - targetPos.x);
    if (!canStep(owner.getEntity(), direction)) {
      movementTask.stop();
      status = Status.FINISHED;
      repositionRequired = false;
      return;
    }

    movementTask.update();

    if (movementTask.getStatus() != Status.ACTIVE) {
      status = Status.FINISHED;
      repositionRequired = false;
    }
  }

  /**
   * @return the unit direction to back off in. On the ground that is straight along the floor.
   */
  protected Vector2 directionAwayFrom(Vector2 enemyPos, Vector2 targetPos) {
    Vector2 away = enemyPos.cpy().sub(targetPos);
    away.y = 0;
    return away.nor();
  }

  /**
   * @param direction 1 to step right, -1 to step left
   * @return false if the enemy must not step that way. On the ground that is when there is no floor
   *     ahead, so it does not back off the edge of a platform.
   */
  protected boolean canStep(Entity enemy, float direction) {
    if (edgeDetector == null) {
      edgeDetector = new EdgeDetectTask(0.3f);
    }
    return edgeDetector.isGroundAhead(enemy, direction);
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
