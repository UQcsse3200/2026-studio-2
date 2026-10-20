package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public class EnemyDeathComponent extends Component {
  private boolean dead = false;

  /* Create, with a listener for health updates. */
  @Override
  public void create() {
    entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
  }

  /* Execute enemy behaviour for a given health update. */
  private void onHealthUpdate(int enemyHealth) {

    // Handle enemy death
    if (enemyHealth <= 0 && !dead) {
      dead = true;
      SoundEffects.play("sounds/enemy_Death.wav", 0.4f);
      entity.getEvents().trigger("enemyDied", entity);

      // Special behaviour for summoned enemies
      EnemySummonComponent summonComponent = entity.getComponent(EnemySummonComponent.class);
      if (summonComponent != null && summonComponent.getSummonedStatus()) {
        Entity summoner = summonComponent.getSummoner();
        summoner.getEvents().trigger("summonedEnemyDied");
      }

      ServiceLocator.getEntityService().scheduleRemoval(entity);
    }
  }
}
