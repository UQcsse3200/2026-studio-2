package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

/** Close-range attack that damages the first enemy directly in front of the player. */
public class MeleeComponent extends Component implements AttackBehaviour {
  private static final float RANGE = 1.2f; // how far the melee reaches

  @Override
  public void create() {
    entity.getEvents().addListener("melee", this::attack);
  }

  @Override
  public void attack(Vector2 direction) {
    if (direction == null || direction.isZero()) {
      return;
    }

    Vector2 origin = entity.getCenterPosition();
    Vector2 target = origin.cpy().mulAdd(direction.cpy().nor(), RANGE);

    PhysicsEngine physics = ServiceLocator.getPhysicsService().getPhysics();
    RaycastHit hit = new RaycastHit();
    if (!physics.raycast(origin, target, PhysicsLayer.NPC, hit)) {
      return;
    }

    Object userData = hit.fixture.getBody().getUserData();
    if (!(userData instanceof BodyUserData)) {
      return;
    }
    Entity hitEntity = ((BodyUserData) userData).entity;
    if (hitEntity == null) {
      return;
    }

    CombatStatsComponent targetStats = hitEntity.getComponent(CombatStatsComponent.class);
    if (targetStats != null) {
      targetStats.hit(entity.getComponent(CombatStatsComponent.class));
    }
  }
}
