package com.csse3200.game.components;

import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

public class PoisonStatsComponent extends Component {
  private static final long DAMAGE_INTERVAL = 1000L;
  private CombatStatsComponent combatStats;
  private float damagePerSecond;
  private long poisonEndTime;
  private long nextDamageTime;

  @Override
  public void create() {
    combatStats = entity.getComponent(CombatStatsComponent.class);
    entity.getEvents().addListener("applyPoison", this::applyPoison);
  }

  private void applyPoison(float damagePerSecond, float durationSeconds) {
    if (damagePerSecond <= 0f || durationSeconds <= 0f) {
      return;
    }
    GameTime time = ServiceLocator.getTimeSource();
    if (time == null) {
      return;
    }
    long currentTime = time.getTime();
    this.damagePerSecond = damagePerSecond;
    this.poisonEndTime = currentTime + (long) (durationSeconds * 1000f);
    this.nextDamageTime = currentTime + DAMAGE_INTERVAL;
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
    while (currentTime >= nextDamageTime && nextDamageTime <= poisonEndTime) {
      combatStats.addHealth(-Math.round(damagePerSecond));
      nextDamageTime += DAMAGE_INTERVAL;
    }

    if (currentTime >= poisonEndTime && nextDamageTime > poisonEndTime) {
      damagePerSecond = 0f;
    }
  }

  public boolean isPoisoned() {
    return damagePerSecond > 0f;
  }
}
