package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameCommand.class);

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

    String arg = args.get(1);
    switch (arg) {
      case "start":
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("startMinigame");
        return true;
      case "restart":
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("restartMinigame");
        return true;
      case "success":
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("timingSuccess");
        return true;
      case "failure":
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("timingFailure");
        return true;
      case "showBar":
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("showTimingBar");
        return true;
      case "hideBar":
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("hideTimingBar");
        return true;
      default:
        logger.debug("Unrecognised argument received for 'minigame-cyclops command: {}", args);
        return false;
    }
  }

  boolean isValid(ArrayList<String> args) {
    return args.size() == 2;
  }
}
