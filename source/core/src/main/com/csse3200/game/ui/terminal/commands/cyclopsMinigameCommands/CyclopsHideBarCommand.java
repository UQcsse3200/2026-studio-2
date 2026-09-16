package com.csse3200.game.ui.terminal.commands.cyclopsMinigameCommands;

import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.terminal.commands.Command;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsHideBarCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsHideBarCommand.class);

  /**
   * Action a command.
   *
   * @param args command args
   * @return command was successful
   */
  @Override
  public boolean action(ArrayList<String> args) {
    if (!isValid(args)) {
      logger.debug("Invalid arguments received for 'minigame-cyclops' command: {}", args);
      return false;
    }

    ServiceLocator.getCyclopsMinigameEventHandler().trigger("hideBar");
    return true;
  }

  boolean isValid(ArrayList<String> args) {
    return args.isEmpty();
  }
}
