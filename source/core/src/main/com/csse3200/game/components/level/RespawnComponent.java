package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

public class RespawnComponent extends Component {
  /** Health lost when respawning after falling, and penalty applied on checkpoint restart. */
  public static final int RESPAWN_HEALTH_PENALTY = 2;

  boolean respawnQueued = false;
  Vector2 respawnCoordinates;

  @Override
  public void update() {
    if (entity.getPosition().y < -10) {
      applyHealthPenalty(entity);
      entity.getEvents().trigger("respawnAtCheckpoint");
    }

    if (respawnQueued && respawnCoordinates != null) {
      entity.getEvents().trigger("grappleRelease");
      entity.setPosition(respawnCoordinates.x, respawnCoordinates.y);
      respawnQueued = false;
      respawnCoordinates = null;
    }
  }

  public void queueRespawn(Vector2 coords) {
    respawnCoordinates = coords;
    respawnQueued = true;
  }

  /** Applies the standard respawn health penalty to the given entity. */
  public static void applyHealthPenalty(Entity entity) {
    CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
    if (stats == null) {
      return;
    }
    stats.setHealth(stats.getHealth() - RESPAWN_HEALTH_PENALTY);
  }

  /**
   * Revives the given entity to full health minus the respawn penalty. Used when restarting after
   * death (health is 0) so the penalty is still felt without an instant re-death.
   */
  public static void reviveWithPenalty(Entity entity) {
    CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
    if (stats == null) {
      return;
    }
    stats.setHealth(Math.max(1, stats.getMaxHealth() - RESPAWN_HEALTH_PENALTY));
  }
}
