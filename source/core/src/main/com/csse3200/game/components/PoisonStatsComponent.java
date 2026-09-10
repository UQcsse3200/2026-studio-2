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
    GameTime time = ServiceLocator.getTimeSource();

    if (time == null || damagePerSecond <= 0f || durationSeconds <= 0f) {
      return;
    }

    long currentTime = time.getTime();
    this.damagePerSecond = damagePerSecond;
    poisonEndTime = currentTime + (long) (durationSeconds * 1000f);
    nextDamageTime = currentTime + DAMAGE_INTERVAL;
  }

  @Override
  public void update() {
    GameTime time = ServiceLocator.getTimeSource();
    if (combatStats == null || time == null || damagePerSecond <= 0f) {
      return;
    }

    long currentTime = time.getTime();

    while (currentTime >= nextDamageTime
        && nextDamageTime <= poisonEndTime
        && !combatStats.isDead()) {
      combatStats.addHealth(-Math.round(damagePerSecond));
      nextDamageTime += DAMAGE_INTERVAL;
    }

    if (currentTime >= poisonEndTime || combatStats.isDead()) {
      damagePerSecond = 0f;
      poisonEndTime = 0L;
      nextDamageTime = 0L;
    }
  }
}
