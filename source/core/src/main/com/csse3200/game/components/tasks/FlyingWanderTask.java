package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;

/**
 * Wanders through the air by moving to a random position within a range of the starting position,
 * waiting a little between movements. There are no platform edges to stop at.
 */
public class FlyingWanderTask extends WanderTask {
  /**
   * @param wanderRange Distance in X and Y the entity can move from its position when start() is
   *     called.
   * @param waitTime How long in seconds to wait between wandering.
   */
  public FlyingWanderTask(Vector2 wanderRange, float waitTime) {
    super(wanderRange, waitTime);
  }

  @Override
  protected boolean canStep(Entity enemy, float direction) {
    return true;
  }
}
