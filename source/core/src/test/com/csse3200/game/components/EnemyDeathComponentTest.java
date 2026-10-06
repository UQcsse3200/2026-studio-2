package com.csse3200.game.components;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class EnemyDeathComponentTest {

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

  @Test
  void shouldPlayDeathSoundOnlyOnce() {
    EntityService entityService = spy(new EntityService());
    ServiceLocator.registerEntityService(entityService);
    ResourceService resources = mock(ResourceService.class);
    Sound sound = mock(Sound.class);
    when(resources.containsAsset("sounds/enemy_Death.wav", Sound.class)).thenReturn(true);
    when(resources.getAsset("sounds/enemy_Death.wav", Sound.class)).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);
    Entity enemy = new Entity();
    CombatStatsComponent combatStats = new CombatStatsComponent(100, 10);
    enemy.addComponent(combatStats);
    enemy.addComponent(new EnemyDeathComponent());
    enemy.create();

    combatStats.setHealth(0);
    combatStats.setHealth(0);

    verify(sound, times(1)).play(0.4f);
    verify(entityService, times(1)).scheduleRemoval(enemy);
  }
}
