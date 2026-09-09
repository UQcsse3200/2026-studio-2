package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

public class PoisonBuff extends Component {
  private float poisonDamagePerSecond;
  private float poisonDuration;
  private float poisonBuffEndTime;

  @Override
  public void create() {
    entity.getEvents().addListener("poisonPotionUsed", this::applyPoisonBuff);
  }

  private void applyPoisonBuff(float poisonDamagePerSecond, float poisonDuration) {
    GameTime time = ServiceLocator.getTimeSource();
    if (time == null || poisonDamagePerSecond < 0f || poisonDuration < 0f) {
      return;
    }

    this.poisonDamagePerSecond = poisonDamagePerSecond;
    this.poisonDuration = poisonDuration;
    poisonBuffEndTime = time.getTime() + (long) (poisonDuration * 1000f);
  }

  public boolean isActive() {
    GameTime time = ServiceLocator.getTimeSource();
    if (time != null && poisonDamagePerSecond > 0f && time.getTime() < poisonBuffEndTime) {
      return true;
    } else {
      return false;
    }
  }

  public float getPoisonDamagePerSecond() {
    return isActive() ? poisonDamagePerSecond : 0f;
  }

  public float getPoisonDuration() {
    return isActive() ? poisonDuration : 0f;
  }
}
