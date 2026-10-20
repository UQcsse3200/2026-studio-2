package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import java.util.Random;

/**
 * Teleports an enemy whenever damage is taken (after short delay), dealt (instantly), or elapsed
 * time.
 *
 * <p>Randomly chooses from a list of predetermined teleport positions.
 */
public class EnemyTeleportComponent extends Component {
  private final List<Vector2> tpPositions;
  private final long damagedTpDelay;
  private final long tpFreq;
  private final Random random;
  private final long hitTpCooldown;
  private long nextHitTpTime;

  private long damagedTpTime;
  private long nextTpTime;
  private Vector2 currentTpPosition;
  private int prevHealth;
  private boolean laserActive = false;

  /**
   * @param tpPositions predetermined teleport locations available to enemy
   * @param damagedTpDelay delay before teleporting after taking damage (milliseconds)
   * @param tpFreq frequency of periodic teleports (milliseconds)
   */
  public EnemyTeleportComponent(List<Vector2> tpPositions, long damagedTpDelay, long tpFreq, long hitTpCooldown) {
    this.tpPositions = tpPositions;
    this.damagedTpDelay = damagedTpDelay;
    this.tpFreq = tpFreq;
    this.hitTpCooldown = hitTpCooldown;
    this.random = new Random();

    damagedTpTime = -1;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("hitPlayer", this::hitTeleport);
    entity.getEvents().addListener("updateHealth", this::damagedTeleport);
    entity.getEvents().addListener("laserStart", () -> laserActive = true);
    entity.getEvents().addListener("laserEnd", () -> laserActive = false);

    nextTpTime = ServiceLocator.getTimeSource().getTime() + tpFreq;
    prevHealth =
        entity
            .getComponent(CombatStatsComponent.class)
            .getHealth(); // Store enemy previous health for determining health change
  }

  @Override
  public void update() {

    BossPhaseComponent phase = entity.getComponent(BossPhaseComponent.class);

    if (phase != null && phase.isTransitioning()) {
      return;
    }

    if (laserActive) {
      return;
    }

    long currentTime = ServiceLocator.getTimeSource().getTime();

    if ((damagedTpTime >= 0) && (currentTime >= damagedTpTime)) {
      damagedTpTime = -1;
      teleport();
      nextTpTime = currentTime + tpFreq;
      return;
    }
    if (currentTime >= nextTpTime) {
      teleport();
      nextTpTime = currentTime + tpFreq;
    }
  }

  private void damagedTeleport(int health) {
    if (health >= prevHealth) {
      return;
    }
    prevHealth = health;

    long currentTime = ServiceLocator.getTimeSource().getTime();
    damagedTpTime = currentTime + damagedTpDelay;
  }

  private void hitTeleport() {
    long currentTime = ServiceLocator.getTimeSource().getTime();

    if (laserActive) {
      return;
    }

    if (currentTime < nextHitTpTime) {
      return;
    }
    nextHitTpTime = currentTime + hitTpCooldown;
    teleport();
  }

  private void teleport() {
    if (tpPositions.isEmpty()) {
      return;
    }

    List<Vector2> availableTpPositions =
        tpPositions.stream().filter(pos -> !pos.equals(currentTpPosition)).toList();
    if (availableTpPositions.isEmpty()) {
      return; // the only position is the one it is already at
    }
    currentTpPosition = availableTpPositions.get(random.nextInt(availableTpPositions.size()));
    entity.setPosition(currentTpPosition);

    PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);

    if (physics != null) {
      physics.getBody().setLinearVelocity(0f, 0f);
      physics.getBody().setAngularVelocity(0f);
    }
    // sprint 4: RUN TPed ANIMATION (no charge up time for teleport so animation is based on having
    // tped)
  }
}
