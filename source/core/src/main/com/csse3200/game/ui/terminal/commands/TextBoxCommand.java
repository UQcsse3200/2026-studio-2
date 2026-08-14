package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.ui.dialogue.Text;
import com.csse3200.game.ui.dialogue.TextBox;
import java.util.ArrayList;

public class TextBoxCommand implements Command {
  private final Text message;

  public TextBoxCommand(String message) {
    this.message = new Text(message);
  }

  public void execute() {
    System.out.println(message.getContent());
    TextBox textBox = new TextBox(message);
    textBox.create();
  }

  @Override
  public boolean action(ArrayList<String> args) {
    execute();
    return true;
  }
}