package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.entities.factories.TextBoxFactory;
import java.util.ArrayList;

public class TextBoxCommand implements Command {

  private final String filePath;

  public TextBoxCommand(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public boolean action(ArrayList<String> args) {
    TextBoxFactory textBoxFactory = new TextBoxFactory();
    textBoxFactory.createTextBox(this.filePath);
    return true;
  }
}
