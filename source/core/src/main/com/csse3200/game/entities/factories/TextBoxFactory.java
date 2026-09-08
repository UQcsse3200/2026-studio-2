package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.entities.configs.TextConfig;
import com.csse3200.game.files.FileLoader;

public class TextBoxFactory {
  public static final TextConfig textConfig =
      FileLoader.readClass(TextConfig.class, "configs/textBoxes.json");

  /** Takes in an already created TextConfig file and extracts the parameters */
  public TextBoxFactory() {
    // this.textConfig = FileLoader.readClass(TextConfig.class, "configs/textBoxes.json");
  }

  /**
   * @param filePath the path from source. e.g: configs/textBoxes.json
   */
  public void createTextBox(String filePath) {
    try {
      new TextBoxComponent(
          textConfig.xPos,
          textConfig.yPos,
          Color.WHITE,
          Color.BLACK,
          Color.WHITE,
          textConfig.charsPerSecond,
          textConfig.maxWidth,
          textConfig.padding,
          textConfig.borderThickness,
          textConfig.text).create();
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }
  }
}
