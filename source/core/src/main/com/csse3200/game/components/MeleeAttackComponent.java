package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;
import java.util.LinkedHashSet;
import java.util.Set;

/** Handles melee attacks, sweeping an arc in front of the player. */
public class MeleeAttackComponent extends Component {
  /** Total width of the swing in degrees, centred on the attack direction. */
  private static final float SWEEP_ARC_DEGREES = 120f;

  /** Rays fanned across the arc. More rays means fewer gaps at long range. */
  private static final int SWEEP_RAY_COUNT = 13;

  @Override
  public void create() {
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
    Vector2 forward = direction.cpy().nor();
    Set<Entity> targets = new LinkedHashSet<>();

    for (int i = 0; i < SWEEP_RAY_COUNT; i++) {
      // Spread rays evenly from -arc/2 to +arc/2
      float t = SWEEP_RAY_COUNT == 1 ? 0.5f : (float) i / (SWEEP_RAY_COUNT - 1);
      float angle = (t - 0.5f) * SWEEP_ARC_DEGREES;
      Vector2 end = start.cpy().mulAdd(forward.cpy().rotateDeg(angle), range);

      Entity target = raycastForNpc(start, end);
      if (target != null) {
        targets.add(target);
      }
    }

    for (Entity target : targets) {
      CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
      if (targetStats == null) {
        continue;
      }
      CombatStatsComponent damageSource = new CombatStatsComponent(1, damage);
      target.getEvents().trigger("takeDamage", damageSource);
    }
  }

  /** Returns the NPC entity hit by a ray from start to end, or null if nothing was hit. */
  private Entity raycastForNpc(Vector2 start, Vector2 end) {
    RaycastHit hit = new RaycastHit();
    boolean hitNpc =
            ServiceLocator.getPhysicsService().getPhysics().raycast(start, end, PhysicsLayer.NPC, hit);
    if (!hitNpc || hit.fixture == null) {
      return null;
    }
    Object userData = hit.fixture.getBody().getUserData();
    if (!(userData instanceof BodyUserData)) {
      return null;
    }
    return ((BodyUserData) userData).entity;
  }
}