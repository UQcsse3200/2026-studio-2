package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

/** Handles sword and spear attacks against the closet NPC in the aimed direction */
public class MeleeAttackComponent extends Component {
  private PoisonBuff poisonBuff;

  @Override
  public void create() {
    poisonBuff = entity.getComponent(PoisonBuff.class);
    entity.getEvents().addListener("meleeAttack", this::attack);
  }

  private void attack(Vector2 direction, int damage, float range) {
    if (direction == null
        || direction.isZero()
        || damage <= 0
        || range <= 0f
        || ServiceLocator.getPhysicsService() == null) {
      return;
    }

    Vector2 start = entity.getCenterPosition();
    Vector2 end = start.cpy().mulAdd(direction.cpy().nor(), range);
    RaycastHit hit = new RaycastHit();

    boolean hitNpc =
        ServiceLocator.getPhysicsService().getPhysics().raycast(start, end, PhysicsLayer.NPC, hit);
    if (!hitNpc || hit.fixture == null) {
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

    CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
    if (targetStats == null) {
      return;
    }

    int healthBefore = targetStats.getHealth();

    CombatStatsComponent damageSource = new CombatStatsComponent(1, damage);
    target.getEvents().trigger("takeDamage", damageSource);

    boolean damageWasApplied = targetStats.getHealth() < healthBefore;
    if (damageWasApplied && poisonBuff != null && poisonBuff.isActive()) {
      target
          .getEvents()
          .trigger(
              "applyPoison", poisonBuff.getPoisonDamagePerSecond(), poisonBuff.getPoisonDuration());
    }
  }
}
