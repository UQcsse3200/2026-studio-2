package com.csse3200.game.ui.dialogue;

import java.io.File;

public class Text {
  private final String content;

  public Text(String content) {
    this.content = content;
  }

  public Text(File file) {
    // Load the content from the file
    this.content = loadContentFromFile(file);
  }

  private String loadContentFromFile(File file) {
    try {
      return new String(java.nio.file.Files.readAllBytes(file.toPath()));
    } catch (java.io.IOException e) {
      return "Failed to load file";
    }
  }

  public String getContent() {
    return content;
  }
}