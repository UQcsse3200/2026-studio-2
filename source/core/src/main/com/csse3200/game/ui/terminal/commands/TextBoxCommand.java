package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.entities.configs.TextConfig;
import com.csse3200.game.entities.factories.TextBoxFactory;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextBoxCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(TextBoxCommand.class);
  private static final String PORTRAIT_SUBCOMMAND = "portrait";
  private static final String PORTRAIT_DIRECTORY = "images/portraits/";
  private static final String[] PORTRAIT_EXTENSIONS = {".jpeg", ".jpg", ".png"};

  private final String filePath;

  public TextBoxCommand(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public boolean action(ArrayList<String> args) {
    if (!args.isEmpty() && args.get(0).equalsIgnoreCase(PORTRAIT_SUBCOMMAND)) {
      if (args.size() < 2) {
        logger.error("Usage: textbox portrait <portrait_name>");
        return false;
      }
      return previewPortrait(args.get(1));
    }
    TextBoxFactory textBoxFactory = new TextBoxFactory();
    textBoxFactory.createTextBox(this.filePath);
    return true;
  }

  /** Spawns a single-page preview box for the named portrait. */
  private boolean previewPortrait(String name) {
    String path = resolvePortraitPath(name);
    if (path == null) {
      logger.error("Portrait not found: {}", name);
      return false;
    }
    TextConfig config = new TextConfig();
    config.pages = new ArrayList<>(List.of("Portrait preview: " + path));
    config.portraitPaths = new ArrayList<>(List.of(path));
    new TextBoxFactory().createTextBox(config);
    return true;
  }

  /**
   * Resolves a portrait name to an asset path. Explicit paths (containing a slash or an image
   * extension) are used as-is when they exist; otherwise the name is looked up under the portraits
   * directory, trying each supported extension in order.
   *
   * @return the resolved asset path, or null when nothing matches
   */
  static String resolvePortraitPath(String name) {
    if (name == null || name.isBlank()) {
      return null;
    }
    String trimmed = name.trim();
    if (trimmed.contains("/") || trimmed.contains(".")) {
      return assetExists(trimmed) ? trimmed : null;
    }
    for (String extension : PORTRAIT_EXTENSIONS) {
      String candidate = PORTRAIT_DIRECTORY + trimmed + extension;
      if (assetExists(candidate)) {
        return candidate;
      }
    }
    return null;
  }

  private static boolean assetExists(String path) {
    try {
      return Gdx.files.internal(path).exists();
    } catch (Exception e) {
      return false;
    }
  }
}
