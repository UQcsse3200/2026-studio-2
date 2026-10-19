package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.components.TutorialComponent;
import com.csse3200.game.entities.configs.TextConfig;
import com.csse3200.game.files.FileLoader;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts the movement tutorial from the debug terminal.
 *
 * <p>Format: {@code tutorial}. The tutorial textbox is anchored bottom-left beside the inventory
 * hotbar and advances on A+D, SPACE, then SHIFT key presses. Re-running the command restarts it.
 */
public class TutorialCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(TutorialCommand.class);
  private static final String DEFAULT_CONFIG = "configs/tutorial.json";

  private final String configPath;
  private TutorialComponent activeTutorial;

  public TutorialCommand() {
    this(DEFAULT_CONFIG);
  }

  public TutorialCommand(String configPath) {
    this.configPath = configPath;
  }

  @Override
  public boolean action(ArrayList<String> args) {
    String path = configPath;
    if (args != null && args.size() == 1 && !args.get(0).isBlank()) {
      path = args.get(0);
    }

    TextConfig config = FileLoader.readClass(TextConfig.class, path);
    if (config == null) {
      logger.error("Failed to load tutorial config from {}", path);
      return false;
    }

    restartTutorial(config);
    return true;
  }

  private void restartTutorial(TextConfig config) {
    if (activeTutorial != null) {
      activeTutorial.dispose();
      activeTutorial = null;
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

    activeTutorial = new TutorialComponent(textBox);
    activeTutorial.create();
  }
}
