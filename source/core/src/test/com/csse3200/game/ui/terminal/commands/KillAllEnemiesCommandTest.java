package com.csse3200.game.ui.terminal.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.EnemyDeathComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class KillAllEnemiesCommandTest {

  @Test
  void shouldKillAllEnemies() {
    EntityService entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);

    Entity firstEnemy = new Entity();
    Entity secondEnemy = new Entity();

    // Note: Enemies are identified by cmd via having EnemyDeathComponent
    firstEnemy.addComponent(new CombatStatsComponent(100, 10));
    firstEnemy.addComponent(new EnemyDeathComponent());
    secondEnemy.addComponent(new CombatStatsComponent(80, 20));
    secondEnemy.addComponent(new EnemyDeathComponent());

    entityService.register(firstEnemy);
    entityService.register(secondEnemy);

    KillAllEnemiesCommand cmd = new KillAllEnemiesCommand();
    cmd.action(new ArrayList<>());

    assertEquals(0, firstEnemy.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(0, secondEnemy.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void shouldNotKillNonEnemies() {
    EntityService entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);

    Entity notEnemy = new Entity();

    notEnemy.addComponent(new CombatStatsComponent(100, 10));
    // Note: NonEnemies wont have EnemyDeathComponent

    entityService.register(notEnemy);

    KillAllEnemiesCommand cmd = new KillAllEnemiesCommand();
    cmd.action(new ArrayList<>());

    assertEquals(100, notEnemy.getComponent(CombatStatsComponent.class).getHealth());
  }
}
