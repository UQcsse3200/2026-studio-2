package com.csse3200.game.components;

import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/** Applies burning damage over time to an entity. */
public class BurnStatsComponent extends Component {
  private static final long DAMAGE_INTERVAL = 1000L;
  private CombatStatsComponent combatStats;
  private float damagePerSecond;
  private long burnEndTime;
  private long nextDamageTime;

  @Override
  public void create() {
    combatStats = entity.getComponent(CombatStatsComponent.class);
    entity.getEvents().addListener("applyBurn", this::applyBurn);
  }

  @Override
  public void update() {
    if (combatStats == null || damagePerSecond <= 0f) {
      return;
    }

    GameTime time = ServiceLocator.getTimeSource();
    if (time == null) {
      return;
    }

    long currentTime = time.getTime();

    // Deal every missed one-second tick, including the final tick at burnEndTimeMs.
    while (currentTime >= nextDamageTime && nextDamageTime <= burnEndTime) {
      combatStats.addHealth(-Math.round(damagePerSecond));
      nextDamageTime += DAMAGE_INTERVAL;
    }

    if (currentTime >= burnEndTime && nextDamageTime > burnEndTime) {
      damagePerSecond = 0f;
    }
  }

  /**
   * Starts or refreshes the burn. Reapplying fire resets the five-second duration; it does not
   * stack damage.
   */
  public void applyBurn(float damagePerSecond, float durationSeconds) {
    if (damagePerSecond <= 0f || durationSeconds <= 0f) {
      return;
    }

    GameTime time = ServiceLocator.getTimeSource();
    if (time == null) {
      return;
    }

    long currentTime = time.getTime();
    this.damagePerSecond = damagePerSecond;
    this.burnEndTime = currentTime + (long) (durationSeconds * 1000f);
    this.nextDamageTime = currentTime + DAMAGE_INTERVAL;
  }

  public boolean isBurning() {
    return damagePerSecond > 0f;
  }
}
