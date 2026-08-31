package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.GameEndState;
import java.util.ArrayList;

/** Triggers the lose state through the existing event bus. */
public class GameEndLoseCommand implements Command {
  @Override
  public boolean action(ArrayList<String> args) {
    if (!args.isEmpty()) {
      return false;
    }
    ServiceLocator.getGameEndEventHandler().trigger("gameEnd", GameEndState.LOSE);
    return true;
  }
}
