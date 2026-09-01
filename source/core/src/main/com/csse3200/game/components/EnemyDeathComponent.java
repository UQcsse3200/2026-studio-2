package com.csse3200.game.components;

import com.csse3200.game.services.ServiceLocator;

public class EnemyDeathComponent extends Component {

  /* Create, with a listener for health updates. */
  @Override
  public void create() {
    entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
  }

  /* Execute enemy behaviour for a given health update. */
  private void onHealthUpdate(int enemyHealth) {

    // Handle enemy death
    if (enemyHealth <= 0) {
      ServiceLocator.getEntityService().scheduleRemoval(entity);
    }
  }

  // @Override ...  super.dispose();
  // dispose of event listener?
}
