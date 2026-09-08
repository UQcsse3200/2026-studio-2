package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.entities.configs.TextConfig;
import com.csse3200.game.files.FileLoader;

public class TextBoxFactory {

  /** Takes in an already created TextConfig file and extracts the parameters */
  public TextBoxFactory() {}

  /**
   * @param filePath the path from source. e.g: configs/textBoxes.json
   */
  public static void createTextBox(String filePath) {
    try {
      TextConfig config = FileLoader.readClass(TextConfig.class, filePath);
      if (config == null) {
        System.err.println("Error: Could not load TextConfig from " + filePath);
        return;
      }

      new TextBoxComponent(
          config.xPos,
          config.yPos,
          Color.WHITE,
          Color.BLACK,
          Color.WHITE,
          config.charsPerSecond,
          config.maxWidth,
          config.padding,
          config.borderThickness,
          config.text);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
