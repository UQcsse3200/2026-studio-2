package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;

/** A Devtool that opens Spin the Wheel by triggering "spinTheWheel" on the player. */
public class SpinTheWheelCommand implements Command {
  @Override
  public boolean action(ArrayList<String> args) {
    if (!args.isEmpty()) {
      return false;
    }

    for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
      if (entity.getComponent(PlayerActions.class) != null) {
        entity.getEvents().trigger("spinTheWheel");
        return true;
      }
    }
    return false;
  }
}
