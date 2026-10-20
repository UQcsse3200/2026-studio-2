package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    verify(sound, times(1)).play(0.6f);
    verify(entityService, times(1)).scheduleRemoval(enemy);
  }

  @Test
  void shouldPlayItsOwnDeathSoundOnlyOnce() {
    EntityService entityService = spy(new EntityService());
    ServiceLocator.registerEntityService(entityService);
    ResourceService resources = mock(ResourceService.class);
    Sound sound = mock(Sound.class);
    when(resources.containsAsset("sounds/skeleton_death.ogg", Sound.class)).thenReturn(true);
    when(resources.getAsset("sounds/skeleton_death.ogg", Sound.class)).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);
    Entity enemy = new Entity();
    CombatStatsComponent combatStats = new CombatStatsComponent(100, 10);
    enemy.addComponent(combatStats);
    enemy.addComponent(new EnemyDeathComponent("sounds/skeleton_death.ogg"));
    enemy.create();

    combatStats.setHealth(0);
    combatStats.setHealth(0);

    verify(sound, times(1)).play(0.6f);
    verify(resources, never()).getAsset(EnemyDeathComponent.DEFAULT_DEATH_SOUND, Sound.class);
  }

  @Test
  void shouldDefaultToTheGenericDeathSound() {
    assertEquals(
        EnemyDeathComponent.DEFAULT_DEATH_SOUND, new EnemyDeathComponent().getDeathSound());
  }
}
