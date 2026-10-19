package com.csse3200.game.components.tasks;

import com.csse3200.game.entities.Entity;

/** Chases a target entity through the air, so there are no platform edges to stop at. */
public class FlyingChaseTask extends ChaseTask {
  public FlyingChaseTask(Entity target, int priority, float viewDistance, float maxChaseDistance) {
    super(target, priority, viewDistance, maxChaseDistance);
  }

  @Override
  protected boolean canAdvance(Entity enemy, float direction) {
    return true;
  }
}
