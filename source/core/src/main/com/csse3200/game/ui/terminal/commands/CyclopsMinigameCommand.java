package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.cutscene.CutsceneLoader;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameCommand.class);

  /** Maps each terminal argument to the cyclops minigame event it triggers. */
  private static final Map<String, String> COMMAND_EVENTS =
      Map.of(
          "load", "load",
          "success", "success",
          "failure", "failure",
          "stop", "stop",
          "start", "start",
          "restart", "restart",
          "show", "showBar",
          "hide", "hideBar");

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
              CutsceneLoader.Result result = loader.load("cutscene2");
              if (!result.isSuccess()) {
                logger.debug("Could not start cutscene '{}': {}", "cutscene2", result.getError());
              } else {
                game.startCutscene(result.getCutscene(), GdxGame.ScreenType.CYCLOPS_MINIGAME);
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
    String event = COMMAND_EVENTS.get(arg);
    if (event == null) {
      logger.info("Invalid argument received for 'cyclopsMinigame' command: {}", arg);
      return false;
    }

    logger.info("Running cyclopsMinigame {} command", arg);
    ServiceLocator.getCyclopsMinigameEventHandler().trigger(event);
    return true;
  }

  boolean isValid(ArrayList<String> args) {
    return args.size() == 1;
  }
}
