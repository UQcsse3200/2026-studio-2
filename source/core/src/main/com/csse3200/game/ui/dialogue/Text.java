package com.csse3200.game.ui.dialogue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Text {
  private String content = "";

  public Text(String content) {
    this.content = content;
  }

  public Text(File file) {
    // Load the content from the file
    this.content = loadContentFromFile(file);
  }

  private String loadContentFromFile(File file) {
    String fileContents = "";
    try {
      fileContents = Files.readString(file.toPath());
    } catch (IOException e) {
      fileContents = "IO exception";
    } catch (OutOfMemoryError e) {
      fileContents = "Out of memory";
    } catch (SecurityException e) {
      fileContents = "Security Error";
    }
    return fileContents;
  }

  public String getContent() {
    if (this.content.isEmpty()) {
      return "Error -1. Empty content variable";
    } else {
      return content;
    }
  }
}
