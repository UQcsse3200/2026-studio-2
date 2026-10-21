package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.components.TutorialComponent;
import com.csse3200.game.entities.configs.TutorialConfig;
import com.csse3200.game.entities.factories.TutorialFactory;
import com.csse3200.game.files.FileLoader;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts a named tutorial sequence from the debug terminal.
 *
 * <p>Format: {@code tutorial [name]}. A bare name loads {@code configs/<name>.json}, e.g. {@code
 * tutorial tut2}; no argument defaults to the movement tutorial {@code tut1}. An argument
 * containing a path separator or a {@code .json} suffix is loaded as an explicit path instead. Each
 * config pairs its pages with input steps; re-running the command restarts the sequence.
 */
public class TutorialCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(TutorialCommand.class);
  private static final String JSON_EXTENSION = ".json";
  private static final String DEFAULT_TUTORIAL = "tut1";
  private static final String TUTORIAL_DIRECTORY = "configs/";

  private final String configPath;
  private TutorialComponent activeTutorial;

  public TutorialCommand() {
    this.configPath = null;
  }

  /** Starts the tutorial loaded from an explicit config path. */
  public TutorialCommand(String configPath) {
    this.configPath = configPath;
  }

  @Override
  public boolean action(ArrayList<String> args) {
    String path = configPath != null ? configPath : resolvePath(args);

    TutorialConfig config = FileLoader.readClass(TutorialConfig.class, path);
    if (config == null) {
      logger.error("Failed to load tutorial config from {}", path);
      return false;
    }

    restartTutorial(path, config);
    return activeTutorial != null;
  }

  /**
   * Resolves the command argument to a config path: a bare name maps to {@code configs/<name>.json}
   * while an explicit path is used verbatim, and a missing argument selects the default tutorial.
   */
  private String resolvePath(ArrayList<String> args) {
    if (args == null || args.isEmpty() || args.get(0) == null || args.get(0).isBlank()) {
      return TUTORIAL_DIRECTORY + DEFAULT_TUTORIAL + JSON_EXTENSION;
    }
    String name = args.get(0).trim();
    if (name.contains("/") || name.endsWith(JSON_EXTENSION)) {
      return name;
    }
    return TUTORIAL_DIRECTORY + name + JSON_EXTENSION;
  }

  private void restartTutorial(String path, TutorialConfig config) {
    if (activeTutorial != null) {
      activeTutorial.dispose();
      activeTutorial = null;
    }

    activeTutorial = TutorialFactory.start(config, null);
    if (activeTutorial == null) {
      logger.error("Could not start tutorial from {}", path);
    }
  }
}
