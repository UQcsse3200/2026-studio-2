package com.csse3200.game.components;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class enemydeathcomponenttest {

  @Test
  void shouldDisposeEnemyAtZeroHealth() {
    EntityService entityService = spy(new EntityService());
    ServiceLocator.registerEntityService(entityService);
    Entity enemy = new Entity();

    CombatStatsComponent combatStats = new CombatStatsComponent(100, 10);
    enemy.addComponent(combatStats);
    enemy.addComponent(new EnemyDeathComponent());
    enemy.create();

    combatStats.setHealth(0);

    verify(entityService).scheduleRemoval(enemy);
  }

  @Test
  void shouldNotDisposeEnemyAtPositiveHealth() {
    EntityService entityService = spy(new EntityService());
    ServiceLocator.registerEntityService(entityService);
    Entity enemy = new Entity();

    CombatStatsComponent combatStats = new CombatStatsComponent(100, 10);

    enemy.addComponent(combatStats);
    enemy.addComponent(new EnemyDeathComponent());
    enemy.create();

    combatStats.setHealth(50);

    verify(entityService, times(0)).scheduleRemoval(enemy);
  }
}
