package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.cutscene.CutsceneLoader;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameCommand.class);

  public CyclopsMinigameCommand(GdxGame game) {
    super();
    if (ServiceLocator.getCyclopsMinigameEventHandler() == null) {
      ServiceLocator.registerCyclopsMinigameEventHandler(new EventHandler());
    }
    ServiceLocator.getCyclopsMinigameEventHandler()
        .addListener(
            "load",
            () -> {
              Entity player = null;
              for (Entity e : ServiceLocator.getEntityService().getEntities()) {
                if (e.getComponent(PlayerActions.class) != null) {
                  player = e;
                  break;
                }
              }
              game.savePlayerState(player);

              CutsceneLoader loader = new CutsceneLoader();
              CutsceneLoader.Result result = loader.load("cutscene3");
              if (!result.isSuccess()) {
                logger.debug("Could not start cutscene '{}': {}", "cutscene3", result.getError());
              } else {
                game.startCutscene(result.getCutscene(), GdxGame.ScreenType.LEVEL_2_GAME);
              }
            });
  }

  @Override
  public boolean action(ArrayList<String> args) {
    logger.info("Args: {}", args);
    if (!isValid(args)) {
      logger.debug("Invalid arguments received for 'cyclopsMinigame' command: {}", args);
      return false;
    }

    if (ServiceLocator.getCyclopsMinigameEventHandler() == null) {
      logger.debug("CyclopsMinigameEventHandler needs to be registered as a service");
      return false;
    }

    String arg = args.getFirst();
    switch (arg) {
      case "load" -> {
        logger.info("Running load ({}) command", arg);
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("load");
      }
      case "success" -> {
        logger.info("Running success ({}) command", arg);
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("success");
      }
      case "failure" -> {
        logger.info("Running failure ({}) command", arg);
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("failure");
      }
      case "stop" -> {
        logger.info("Running stop ({}) command", arg);
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("stop");
      }
      case "start" -> {
        logger.info("Running start ({}) command", arg);
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("start");
      }
      case "restart" -> {
        logger.info("Running restart ({}) command", arg);
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("restart");
      }
      case "show" -> {
        logger.info("Running show ({}) command", arg);
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("showBar");
      }
      case "hide" -> {
        logger.info("Running hide ({}) command", arg);
        ServiceLocator.getCyclopsMinigameEventHandler().trigger("hideBar");
      }
      default -> {
        logger.info("Invalid argument received for 'cyclopsMinigame' command: {}", arg);
        return false;
      }
    }

    return true;
  }

  boolean isValid(ArrayList<String> args) {
    return args.size() == 1;
  }
}
