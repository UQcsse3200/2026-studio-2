package com.csse3200.game.entities.factories;

import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.entities.configs.Portrait;
import com.csse3200.game.entities.configs.TextConfig;
import com.csse3200.game.files.FileLoader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextBoxFactory {
  private static final Logger logger = LoggerFactory.getLogger(TextBoxFactory.class);

  public TextBoxFactory() {
    // Instances hold no state; createFromConfig uses the registered services.
  }

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
    createTextBox(textConfig);
  }

  /**
   * Creates a text box from an in-memory config (e.g. built by a terminal command preview).
   *
   * @param textConfig the text box config to display
   */
  public void createTextBox(TextConfig textConfig) {
    if (textConfig == null) {
      logger.error("Failed to create text box from null config");
      return;
    }

    try {
      new TextBoxComponent(
              new TextBoxComponent.Layout(
                  textConfig.xPos,
                  textConfig.yPos,
                  textConfig.maxWidth,
                  textConfig.padding,
                  textConfig.getTextAlignment()),
              new TextBoxComponent.Style(
                  textConfig.getTextColour(),
                  textConfig.getBackgroundColour(),
                  textConfig.getBorderColour(),
                  textConfig.borderThickness,
                  textConfig.fontPath),
              textConfig.charsPerSecond,
              textConfig.pages,
              textConfig.portraitPaths)
          .create();
    } catch (Exception e) {
      logger.error("Failed to create text box: {}", e.getMessage());
    }
  }

  /**
   * Creates a single-page text box with a character portrait.
   *
   * @param text text to display in the box
   * @param portrait portrait to display above the box
   */
  public void createPortraitTextBox(String text, Portrait portrait) {
    if (text == null || portrait == null) {
      logger.error("Failed to create portrait text box: text and portrait are required");
      return;
    }

    TextConfig textConfig = new TextConfig();
    textConfig.pages = List.of(text);
    textConfig.portraitPaths = List.of(portrait.getAssetPath());
    createTextBox(textConfig);
  }

  /**
   * Creates the externally controlled tutorial textbox with a portrait and no caller-supplied text.
   * Uses the same default {@link TextConfig} layout as the regular portrait textbox factory.
   *
   * @param portrait portrait to display for every tutorial page
   * @return the created textbox, or {@code null} if the portrait is invalid
   */
  public TextBoxComponent createPortraitTextBox(Portrait portrait) {
    if (portrait == null) {
      logger.error("Failed to create tutorial portrait text box: portrait is required");
      return null;
    }

    TextConfig config = new TextConfig();
    config.pages = List.of("Move around using A and D.", "Jump using SPACE.", "Dash using SHIFT.");

    int pageCount = config.pages == null || config.pages.isEmpty() ? 1 : config.pages.size();
    config.portraitPaths = new ArrayList<>();
    for (int page = 0; page < pageCount; page++) {
      config.portraitPaths.add(portrait.getAssetPath());
    }

    try {
      TextBoxComponent textBox =
          new TextBoxComponent(
              new TextBoxComponent.Layout(
                  config.xPos,
                  config.yPos,
                  config.maxWidth,
                  config.padding,
                  config.getTextAlignment()),
              new TextBoxComponent.Style(
                  config.getTextColour(),
                  config.getBackgroundColour(),
                  config.getBorderColour(),
                  config.borderThickness,
                  config.fontPath),
              config.charsPerSecond,
              config.pages,
              config.portraitPaths);
      textBox.setExternallyControlled(true);
      textBox.setPosition(config.xPos, config.yPos, Align.bottomLeft);
      textBox.create();
      return textBox;
    } catch (Exception e) {
      logger.error("Failed to create tutorial portrait text box: {}", e.getMessage());
      return null;
    }
  }
}
