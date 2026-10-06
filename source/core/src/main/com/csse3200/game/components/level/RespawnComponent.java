package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;

public class RespawnComponent extends Component {
  boolean respawnQueued = false;
  Vector2 respawnCoordinates;

  @Override
  public void update() {
    if (entity.getPosition().y < -10) {
      int health = entity.getComponent(CombatStatsComponent.class).getHealth();
      entity.getComponent(CombatStatsComponent.class).setHealth(health - 2);
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
}
