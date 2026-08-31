package com.csse3200.game.components;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class EnemyDeathComponentTest {

  @Test
  void shouldDisposeEntityAtZeroHealth() {
    Entity entity = spy(Entity.class);
    doNothing().when(entity).dispose();

    CombatStatsComponent combatStats = new CombatStatsComponent(100, 10);

    entity.addComponent(combatStats);
    entity.addComponent(new EnemyDeathComponent());
    entity.create();

    combatStats.setHealth(0);

    verify(entity).dispose();
  }

  @Test
  void shouldNotDisposeEntityAtPositiveHealth() {
    Entity entity = spy(Entity.class);
    doNothing().when(entity).dispose();

    CombatStatsComponent combatStats = new CombatStatsComponent(100, 10);

    entity.addComponent(combatStats);
    entity.addComponent(new EnemyDeathComponent());
    entity.create();

    combatStats.setHealth(50);

    verify(entity, times(0)).dispose();
  }
}
