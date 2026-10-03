package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
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

  private long damagedTpTime;
  private long nextTpTime;
  private Vector2 currentTpPosition;
  private int prevHealth;

  /**
   * @param tpPositions predetermined teleport locations available to enemy
   * @param damagedTpDelay delay before teleporting after taking damage (milliseconds)
   * @param tpFreq frequency of periodic teleports (milliseconds)
   */
  public EnemyTeleportComponent(List<Vector2> tpPositions, long damagedTpDelay, long tpFreq) {
    this.tpPositions = tpPositions;
    this.damagedTpDelay = damagedTpDelay;
    this.tpFreq = tpFreq;
    this.random = new Random();

    damagedTpTime = -1;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("hitPlayer", this::teleport);
    entity.getEvents().addListener("updateHealth", this::damagedTeleport);

    nextTpTime = ServiceLocator.getTimeSource().getTime() + tpFreq;
    prevHealth =
        entity
            .getComponent(CombatStatsComponent.class)
            .getHealth(); // Store enemy previous health for determining health change
  }

  @Override
  public void update() {
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
    prevHealth = health;
    if (health >= prevHealth) {
      return;
    }

    long currentTime = ServiceLocator.getTimeSource().getTime();
    damagedTpTime = currentTime + damagedTpDelay;
  }

  private void teleport() {
    if (tpPositions.isEmpty()) {
      return;
    }

    List<Vector2> availableTpPositions =
        tpPositions.stream().filter(pos -> !pos.equals(currentTpPosition)).toList();
    currentTpPosition = availableTpPositions.get(random.nextInt(availableTpPositions.size()));
    entity.setPosition(currentTpPosition);

    // sprint 4: RUN TPed ANIMATION (no charge up time for teleport so animation is based on having
    // tped)
  }
}
