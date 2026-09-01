package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

/** Close-range attack that damages the first enemy directly in front of the player. */
public class MeleeComponent extends Component implements AttackBehaviour {
  private static final float RANGE = 1.2f;

  @Override
  public void create() {
    entity.getEvents().addListener("melee", this::attack);
  }

  @Override
  public void attack(Vector2 direction) {
    if (direction == null || direction.isZero()) {
      return;
    }

    // Cast a short ray out from the player and stop at the first NPC it touches
    Vector2 origin = entity.getCenterPosition();
    Vector2 reach = origin.cpy().mulAdd(direction.cpy().nor(), RANGE);
    RaycastHit hit = new RaycastHit();

    if (!ServiceLocator.getPhysicsService()
        .getPhysics()
        .raycast(origin, reach, PhysicsLayer.NPC, hit)) {
      return;
    }

    Object userData = hit.fixture.getBody().getUserData();
    if (!(userData instanceof BodyUserData)) {
      return;
    }

    Entity target = ((BodyUserData) userData).entity;
    if (target == null) {
      return;
    }

    // Damage comes from the player's own combat stats
    CombatStatsComponent stats = target.getComponent(CombatStatsComponent.class);
    if (stats != null) {
      stats.hit(entity.getComponent(CombatStatsComponent.class));
    }
  }
}
