package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.GdxGame;
import com.csse3200.game.cutscene.CutsceneLoader;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Starts a named cutscene from the debug terminal. 
 * format: cutscene <cutscene_name>
*/
public class CutsceneCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(CutsceneCommand.class);

  private final GdxGame game;
  private final GdxGame.ScreenType destination;
  private final CutsceneLoader loader;

  public CutsceneCommand(GdxGame game, GdxGame.ScreenType destination, CutsceneLoader loader) {
    this.game = game;
    this.destination = destination;
    this.loader = loader;
  }

  public CutsceneCommand(GdxGame game, GdxGame.ScreenType destination) {
    this(game, destination, new CutsceneLoader());
  }

  @Override
  public boolean action(ArrayList<String> args) {
    if (args == null || args.size() != 1) {
      logger.debug(
          "Expected one cutscene name, received {} arguments", args == null ? 0 : args.size());
      return false;
    }

    CutsceneLoader.Result result = loader.load(args.get(0));
    if (!result.isSuccess()) {
      logger.debug("Could not start cutscene '{}': {}", args.get(0), result.getError());
      return false;
    }

    game.startCutscene(result.getCutscene(), destination);
    return true;
  }
}
