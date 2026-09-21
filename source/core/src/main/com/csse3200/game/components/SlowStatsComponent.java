package com.csse3200.game.components;

import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/** Temporarily reduces an entity's movement speed. */
public class SlowStatsComponent extends Component {
  private PhysicsMovementComponent movementComponent;
  private float slowMultiplier = 1f;
  private long slowEndTime;

  @Override
  public void create() {
    movementComponent = entity.getComponent(PhysicsMovementComponent.class);
    entity.getEvents().addListener("applySlow", this::applySlow);
  }

  @Override
  public void update() {
    if (movementComponent == null || slowMultiplier == 1f) {
      return;
    }

    GameTime time = ServiceLocator.getTimeSource();
    if (time == null) {
      return;
    }

    if (time.getTime() >= slowEndTime) {
      slowMultiplier = 1f;
      movementComponent.setSpeedMultiplier(1f);
    }
  }

  /**
   * Applies or refreshes a slow effect.
   *
   * @param speedMultiplier remaining speed, e.g. 0.75 means 75% speed
   * @param durationSeconds slow duration in seconds
   */
  public void applySlow(float speedMultiplier, float durationSeconds) {
    if (movementComponent == null
        || speedMultiplier <= 0f
        || speedMultiplier >= 1f
        || durationSeconds <= 0f) {
      return;
    }

    GameTime time = ServiceLocator.getTimeSource();
    if (time == null) {
      return;
    }

    slowMultiplier = speedMultiplier;
    slowEndTime = time.getTime() + (long) (durationSeconds * 1000f);
    movementComponent.setSpeedMultiplier(slowMultiplier);
  }

  public boolean isSlowed() {
    return slowMultiplier < 1f;
  }
}
