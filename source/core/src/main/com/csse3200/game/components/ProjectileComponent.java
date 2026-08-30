package com.csse3200.game.components;

import com.csse3200.game.services.ServiceLocator;

public class ProjectileComponent extends Component {

  private float remainingLifetime;

  /**
   * Creates a projectile component.
   *
   * @param lifetime maximum lifetime of the projectile in seconds
   */
  public ProjectileComponent(float lifetime) {
    this.remainingLifetime = lifetime;
  }

  @Override
  public void update() {
    remainingLifetime -= ServiceLocator.getTimeSource().getDeltaTime();

    if (remainingLifetime <= 0f) {
      ServiceLocator.getEntityService().scheduleForDisposal(entity);
    }
  }
}
