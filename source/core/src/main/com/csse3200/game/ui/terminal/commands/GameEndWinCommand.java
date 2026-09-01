package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.GameEndState;
import java.util.ArrayList;

/** Triggers the win state through the existing event bus. */
public class GameEndWinCommand implements Command {
  @Override
  public boolean action(ArrayList<String> args) {
    if (!args.isEmpty()) {
      return false;
    }
    ServiceLocator.getGameEndEventHandler().trigger("gameEnd", GameEndState.WIN);
    return true;
  }
}
