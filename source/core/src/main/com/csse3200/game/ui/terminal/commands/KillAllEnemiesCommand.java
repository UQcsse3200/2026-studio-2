package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.EnemyDeathComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;

/**
 * A Devtool for play-testing convenience that kills all registered enemies
 *
 * <p>Note: Does not protect against unrelated concurrent entity disposal mid-looping *
 */
public class KillAllEnemiesCommand implements Command {
  @Override
  public boolean action(ArrayList<String> args) {

    // loop through entities
    for (Entity entity : ServiceLocator.getEntityService().getEntities()) {

      // filter out non-enemies then kill
      if (entity.getComponent(EnemyDeathComponent.class) != null) {
        CombatStatsComponent enemyStats = entity.getComponent(CombatStatsComponent.class);
        if (enemyStats != null) {
          enemyStats.setHealth(0);
        }
      }
    }

    return true;
  }
}
