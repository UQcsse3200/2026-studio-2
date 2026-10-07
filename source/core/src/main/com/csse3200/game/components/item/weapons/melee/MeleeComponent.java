package com.csse3200.game.components.item.weapons.melee;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Close range melee attack. Starting a swing sweeps an arc of rays out in front of the player, in
 * the direction they are facing, and damages every enemy the arc touches once per swing.
 *
 * <p>Start a swing by triggering the {@code meleeStart} event on the player. When a swing is
 * accepted this component triggers {@code meleeSwing} (with the facing direction, 1 or -1) so the
 * animation can play, and {@code meleeHit} (with the entity hit) for each enemy damaged.
 */
public class MeleeComponent extends Component {
  /** How far the arc reaches from the player's centre, in world units. */
  public static final float RANGE = 1.5f;

  /** Total angle of the arc in degrees, centred on the facing direction. */
  public static final float ARC_DEGREES = 110f;

  /** Number of rays used to sweep the arc. More rays leave fewer gaps at long range. */
  public static final int RAY_COUNT = 9;

  /** Seconds each frame of the melee animation is shown. Raise it to slow the swing down. */
  public static final float FRAME_DURATION = 0.06f;

  /** Number of frames in the melee animation. */
  public static final int FRAME_COUNT = 12;

  /** Seconds the swing lasts. This matches the length of the melee animation. */
  public static final float SWING_DURATION = FRAME_DURATION * FRAME_COUNT;

  /** Seconds from the start of the swing until the arc lands, at about the fifth frame. */
  public static final float HIT_DELAY = FRAME_DURATION * 4.5f;

  /** Seconds after a swing finishes before another can start. */
  public static final float COOLDOWN = 0.15f;

  private boolean swinging = false;
  private boolean hitApplied = false;
  private boolean dead = false;
  private float elapsed = 0f;
  private float cooldownTimer = 0f;
  private int facing = 1;

  @Override
  public void create() {
    super.create();
    entity.getEvents().addListener("meleeStart", this::startSwing);
    entity.getEvents().addListener("death", () -> dead = true);
  }

  @Override
  public void update() {
    if (ServiceLocator.getEntityService() != null
        && ServiceLocator.getEntityService().getPaused()) {
      return;
    }
    float delta = ServiceLocator.getTimeSource().getDeltaTime();

    if (cooldownTimer > 0f) {
      cooldownTimer -= delta;
    }
    if (!swinging) {
      return;
    }

    elapsed += delta;
    if (!hitApplied && elapsed >= HIT_DELAY) {
      hitApplied = true;
      sweep();
    }
    if (elapsed >= SWING_DURATION) {
      swinging = false;
      cooldownTimer = COOLDOWN;
    }
  }

  /** Begins a swing in the direction the player is facing, unless one can't start right now. */
  private void startSwing() {
    if (dead || swinging || cooldownTimer > 0f) {
      return;
    }
    PlayerActions actions = entity.getComponent(PlayerActions.class);
    facing = actions != null && actions.getFacingDirection() < 0 ? -1 : 1;
    swinging = true;
    hitApplied = false;
    elapsed = 0f;
    entity.getEvents().trigger("meleeSwing", facing);
  }

  /** Casts the arc and damages each enemy it touches. */
  private void sweep() {
    CombatStatsComponent attacker = entity.getComponent(CombatStatsComponent.class);
    if (attacker == null) {
      return;
    }
    for (Entity target : findTargets()) {
      CombatStatsComponent stats = target.getComponent(CombatStatsComponent.class);
      if (stats == null || stats.isDead()) {
        continue;
      }
      stats.hit(attacker);
      entity.getEvents().trigger("meleeHit", target);
    }
  }

  /**
   * Finds every enemy inside the arc. Each ray stops at solid ground, so enemies behind a wall
   * can't be hit.
   *
   * @return the distinct entities hit, never including the player
   */
  Set<Entity> findTargets() {
    Set<Entity> targets = new LinkedHashSet<>();
    PhysicsEngine physics = ServiceLocator.getPhysicsService().getPhysics();
    Vector2 origin = entity.getCenterPosition();
    float half = ARC_DEGREES / 2f;

    for (int i = 0; i < RAY_COUNT; i++) {
      float angle = -half + ARC_DEGREES * i / (RAY_COUNT - 1);
      Vector2 end = new Vector2(facing, 0f).rotateDeg(angle).scl(RANGE).add(origin);

      RaycastHit wall = new RaycastHit();
      if (physics.raycast(origin, end, PhysicsLayer.SOLID, wall) && wall.point != null) {
        end = wall.point;
      }

      RaycastHit[] hits = physics.raycastAll(origin, end, PhysicsLayer.NPC);
      for (RaycastHit hit : hits) {
        Entity target = entityOf(hit);
        if (target != null && target != entity) {
          targets.add(target);
        }
      }
    }
    return targets;
  }

  private static Entity entityOf(RaycastHit hit) {
    if (hit == null || hit.fixture == null || hit.fixture.getBody() == null) {
      return null;
    }
    Object data = hit.fixture.getBody().getUserData();
    return data instanceof BodyUserData body ? body.entity : null;
  }

  /**
   * @return true from the start of a swing until it finishes
   */
  public boolean isSwinging() {
    return swinging;
  }

  /**
   * @return how far through the current swing it is, from 0 to 1, or 0 when not swinging
   */
  public float getSwingProgress() {
    return swinging ? Math.min(1f, elapsed / SWING_DURATION) : 0f;
  }

  /**
   * @return 1 if the current (or last) swing was to the right, -1 if to the left
   */
  public int getFacing() {
    return facing;
  }
}
