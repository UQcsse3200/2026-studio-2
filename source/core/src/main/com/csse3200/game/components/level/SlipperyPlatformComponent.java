package com.csse3200.game.components.level;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class SlipperyPlatformComponent extends Component {
  float slipperiness;
  float maxGrappleTime;
  float currentGrappleTime = 0f;
  boolean grappled = false;

  public SlipperyPlatformComponent(float slipperiness, float maxGrappleTime) {
    this.slipperiness = slipperiness;
    this.maxGrappleTime = maxGrappleTime;
  }

  public float getSlipperiness() {
    return slipperiness;
  }

  public void setGrappled(boolean grappled) {
    this.grappled = grappled;
  }

  @Override
  public void update() {
    if (grappled) {
      currentGrappleTime += ServiceLocator.getTimeSource().getDeltaTime();

      if (currentGrappleTime >= maxGrappleTime) {
        entity.getEvents().trigger("grappleTimeExceeded");
        grappled = false;
        currentGrappleTime = 0f;
      }
    }
  }
}
