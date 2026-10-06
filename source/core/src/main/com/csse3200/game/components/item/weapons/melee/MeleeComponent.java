package com.csse3200.game.components.item.weapons.melee;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.item.weapons.PrimaryWeapon;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;
import java.util.LinkedHashSet;
import java.util.Set;

/** Close-range melee attack that sweeps an arc in front of the entity. */
public class MeleeComponent extends Component implements PrimaryWeapon {

  /** Keep in sync with REACH in MeleeRenderComponent so the visual matches the hit area. */
  private static final float RANGE = 1.5f;

  private static final float SWORD_COOLDOWN = 0.3f;

  /** Total width of the swing in degrees, centred on the attack direction. */
  private static final float SWEEP_ARC_DEGREES = 120f;

  /** Rays fanned across the arc. More rays means fewer gaps at long range. */
  private static final int SWEEP_RAY_COUNT = 20;

  private float cooldownTimer = 0f;

  @Override
  public void create() {
    entity.getEvents().addListener("attack", this::attack);
    entity.getEvents().addListener("melee", this::attack);
  }

  @Override
  public void update() {
    if (cooldownTimer > 0f) {
      cooldownTimer -= ServiceLocator.getTimeSource().getDeltaTime();
    }
  }

  @Override
  public void attack(Vector2 direction) {
    if (direction == null || direction.isZero() || !isReady()) {
      return;
    }

    cooldownTimer = SWORD_COOLDOWN;

    Vector2 origin = entity.getCenterPosition();
    Vector2 forward = direction.cpy().nor();
    Set<Entity> targets = new LinkedHashSet<>();

    // Fan rays across the arc and collect each distinct NPC they touch
    for (int i = 0; i < SWEEP_RAY_COUNT; i++) {
      float t = SWEEP_RAY_COUNT == 1 ? 0.5f : (float) i / (SWEEP_RAY_COUNT - 1);
      float angle = (t - 0.5f) * SWEEP_ARC_DEGREES;
      Vector2 reach = origin.cpy().mulAdd(forward.cpy().rotateDeg(angle), RANGE);

      Entity target = raycastForNpc(origin, reach);
      if (target != null) {
        targets.add(target);
      }
    }

    // Damage comes from the entity's own combat stats
    CombatStatsComponent attackerStats = entity.getComponent(CombatStatsComponent.class);
    for (Entity target : targets) {
      CombatStatsComponent stats = target.getComponent(CombatStatsComponent.class);
      if (stats != null) {
        stats.hit(attackerStats);
      }
    }

    // Only announce contact when something was actually hit
    if (!targets.isEmpty()) {
      entity.getEvents().trigger("attackAnimation", forward);
    }
  }

  /** Returns the NPC entity hit by a ray from start to end, or null if nothing was hit. */
  private Entity raycastForNpc(Vector2 start, Vector2 end) {
    RaycastHit hit = new RaycastHit();
    if (!ServiceLocator.getPhysicsService().getPhysics().raycast(start, end, PhysicsLayer.NPC, hit)
            || hit.fixture == null) {
      return null;
    }
    Object userData = hit.fixture.getBody().getUserData();
    if (!(userData instanceof BodyUserData)) {
      return null;
    }
    return ((BodyUserData) userData).entity;
  }

  @Override
  public boolean isReady() {
    return cooldownTimer <= 0f;
  }

  @Override
  public float getCooldownRemaining() {
    return Math.max(0f, cooldownTimer);
  }
}