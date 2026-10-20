package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public class EnemyDeathComponent extends Component {
  public static final String DEFAULT_DEATH_SOUND = "sounds/enemy_Death.wav";
  static final float DEATH_VOLUME = 0.6f;

  private final String deathSound;
  private boolean dead = false;

  public EnemyDeathComponent() {
    this(DEFAULT_DEATH_SOUND);
  }

  public EnemyDeathComponent(String deathSound) {
    this.deathSound = deathSound;
  }

  public String getDeathSound() {
    return deathSound;
  }

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
      SoundEffects.play(deathSound, DEATH_VOLUME);
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
