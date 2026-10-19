package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;

/**
 * Local retry for the upper spike crossing; ordinary descents through the original map are safe.
 */
public class DesertHazardRecoveryComponent extends Component {
  @Override
  public void update() {
    Vector2 position = entity.getPosition();
    CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
    if (stats.isDead()) return;
    boolean upperSpikes =
        position.x > 59.5f && position.x < 68.2f && position.y > 18.5f && position.y < 20.4f;
    boolean fellOut = position.y < -4;
    if (!upperSpikes && !fellOut) return;
    // Use the normal damage/invulnerability path so touching a spike cannot charge twice.
    entity.getEvents().trigger("takeDamage", new CombatStatsComponent(1, 2));
    if (stats.isDead()) return;
    entity.getEvents().trigger("grappleRelease");
    if (upperSpikes) entity.setPosition(53, 21.1f);
    else entity.getEvents().trigger("respawnAtCheckpoint");
    PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
    if (physics != null) {
      physics.getBody().setLinearVelocity(0, 0);
      physics.getBody().setAwake(true);
    }
  }
}
