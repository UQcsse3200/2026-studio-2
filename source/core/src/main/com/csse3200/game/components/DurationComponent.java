package com.csse3200.game.components;

import com.csse3200.game.services.ServiceLocator;

public class DurationComponent extends Component {
  private float remainingDuration;

  public DurationComponent(float duration) {
    this.remainingDuration = duration;
  }

  @Override
  public void update() {
    float deltaTime = ServiceLocator.getTimeSource().getDeltaTime();
    remainingDuration -= deltaTime;

    if (remainingDuration <= 0f) {
      ServiceLocator.getEntityService().scheduleForDisposal(entity);
    }
  }
}
