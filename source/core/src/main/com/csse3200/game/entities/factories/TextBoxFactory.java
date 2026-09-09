package com.csse3200.game.entities.factories;

import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.entities.configs.TextConfig;
import com.csse3200.game.files.FileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextBoxFactory {
  private static final Logger logger = LoggerFactory.getLogger(TextBoxFactory.class);

  public TextBoxFactory() {}

  /**
   * Loads a text box config from the given JSON file and creates the resulting text box.
   *
   * @param filePath the path from source. e.g: configs/textBoxes.json
   */
  public void createTextBox(String filePath) {
    TextConfig textConfig = FileLoader.readClass(TextConfig.class, filePath);
    if (textConfig == null) {
      logger.error("Failed to load text box config from {}", filePath);
      return;
    }

    try {
      new TextBoxComponent(
              textConfig.xPos,
              textConfig.yPos,
              textConfig.getTextColour(),
              textConfig.getBackgroundColour(),
              textConfig.getBorderColour(),
              textConfig.charsPerSecond,
              textConfig.maxWidth,
              textConfig.padding,
              textConfig.borderThickness,
              textConfig.pages)
          .create();
    } catch (Exception e) {
      logger.error("Failed to create text box: {}", e.getMessage());
    }
  }
}
