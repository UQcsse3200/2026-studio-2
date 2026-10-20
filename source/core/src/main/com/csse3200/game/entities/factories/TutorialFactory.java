package com.csse3200.game.entities.factories;

import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.components.TutorialComponent;
import com.csse3200.game.entities.configs.TutorialConfig;
import com.csse3200.game.files.FileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds live tutorial sequences from their configs. Used by both the terminal {@code tutorial}
 * command and programmatic triggers (e.g. the level 1 screen's auto-start and checkpoint-linked
 * tutorials). The caller owns the returned component and should dispose it when the tutorial is no
 * longer needed.
 */
public final class TutorialFactory {
  private static final Logger logger = LoggerFactory.getLogger(TutorialFactory.class);
  private static final String TUTORIAL_DIRECTORY = "configs/";

  private TutorialFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }

  /**
   * Starts the named tutorial, e.g. {@code "tut1"} loads {@code configs/tut1.json}.
   *
   * @param name tutorial name (or an explicit config path containing a separator or .json suffix)
   * @param onFinished callback run when the player completes the final step, or null
   * @return the started tutorial, or null when the config is missing or invalid
   */
  public static TutorialComponent start(String name, Runnable onFinished) {
    String path =
        name.contains("/") || name.endsWith(".json") ? name : TUTORIAL_DIRECTORY + name + ".json";
    TutorialConfig config = FileLoader.readClass(TutorialConfig.class, path);
    if (config == null) {
      logger.error("Failed to load tutorial config from {}", path);
      return null;
    }
    return start(config, onFinished);
  }

  /**
   * Starts a tutorial from an already loaded config.
   *
   * @param config validated tutorial config
   * @param onFinished callback run when the player completes the final step, or null
   * @return the started tutorial, or null when the config is invalid
   */
  public static TutorialComponent start(TutorialConfig config, Runnable onFinished) {
    String problem = config.validateSteps();
    if (problem != null) {
      logger.error("Invalid tutorial config: {}", problem);
      return null;
    }

    TextBoxComponent textBox =
        new TextBoxComponent(
            config.xPos,
            config.yPos,
            config.getTextColour(),
            config.getBackgroundColour(),
            config.getBorderColour(),
            config.charsPerSecond,
            config.maxWidth,
            config.padding,
            config.borderThickness,
            config.fontPath,
            config.getTextAlignment(),
            config.pages,
            config.portraitPaths);
    textBox.setExternallyControlled(true);
    textBox.setPosition(config.xPos, config.yPos, Align.bottomLeft);
    textBox.create();

    TutorialComponent tutorial =
        new TutorialComponent(textBox, config.steps, onFinished, config.autoDismissSeconds);
    tutorial.create();
    return tutorial;
  }
}
