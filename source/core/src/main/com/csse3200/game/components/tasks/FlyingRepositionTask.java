package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;

/** Repositions a flying enemy a distance away from target, in any direction. */
public class FlyingRepositionTask extends RepositionTask {
  public FlyingRepositionTask(Entity target, int priority, float repositionDistance) {
    super(target, priority, repositionDistance);
  }

  @Override
  protected Vector2 directionAwayFrom(Vector2 enemyPos, Vector2 targetPos) {
    return enemyPos.cpy().sub(targetPos).nor();
  }

  @Override
  protected boolean canStep(Entity enemy, float direction) {
    return true;
  }
}
