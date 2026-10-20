package com.csse3200.game.components;

import com.csse3200.game.services.ServiceLocator;

public class BossPhaseComponent extends Component {
  private int currentPhase = 1;
  private boolean phaseChanging = false;
  private long phaseChangingTime;

  private static final long TRANSITION_DURATION = 5000L;

  @Override
  public void create() {
    entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
  }

  private void onHealthUpdate(int health) {
    if (health <= 0 || currentPhase == 2) {
      return;
    }

    CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);

    if (health <= stats.getMaxHealth() / 2f && !phaseChanging) {
      phaseChanging = true;
      phaseChangingTime = ServiceLocator.getTimeSource().getTime();
      entity.getEvents().trigger("beginBossPhaseChange");
    }
  }

  @Override
  public void update() {
    if (phaseChanging
        && ServiceLocator.getTimeSource().getTime() - phaseChangingTime >= TRANSITION_DURATION) {

      phaseChanging = false;
      currentPhase = 2;

      entity.getEvents().trigger("bossPhaseChanged", 2);
    }
  }

  public boolean isTransitioning() {
    return phaseChanging;
  }

  public int getCurrentPhase() {
    return currentPhase;
  }
}
