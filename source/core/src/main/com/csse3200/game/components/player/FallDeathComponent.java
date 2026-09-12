package com.csse3200.game.components.player;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;

/**
 * Kills the entity once it falls below a given world-y threshold, by reusing the existing
 * combat-stats death pipeline rather than a separate game-over path.
 */
public class FallDeathComponent extends Component {
  private final float fallThresholdY;

  public FallDeathComponent(float fallThresholdY) {
    this.fallThresholdY = fallThresholdY;
  }

  @Override
  public void update() {
    if (entity.getPosition().y >= fallThresholdY) {
      return;
    }

    CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
    if (stats != null && !stats.isDead()) {
      stats.setHealth(0);
    }
  }
}
