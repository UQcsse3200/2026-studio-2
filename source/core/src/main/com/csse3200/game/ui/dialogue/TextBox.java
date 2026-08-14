package com.csse3200.game.ui.dialogue;

import com.csse3200.game.components.Component;

public class TextBox extends Component {
  private TextBoxDisplay textBoxDisplay;
  private Text text;

  public TextBox(Text text) {
    this.text = text;
    this.textBoxDisplay = new TextBoxDisplay(text);
  }

  @Override
  public void create() {
    super.create();
    if (textBoxDisplay == null) {
      textBoxDisplay = new TextBoxDisplay(text);
    }
    textBoxDisplay.create();
  }

  /**
   * Show the dialogue box. If a non-null message is provided, update the displayed text.
   * create() on the display is guarded, so calling it here is safe and will register the
   * display with the render service if not already done. Do not call draw() directly; the
   * render service will call draw each frame.
   */
  public void showDialogue(String message) {
    if (message != null) {
      Text newText = new Text(message);
      this.text = newText;
      if (textBoxDisplay == null) textBoxDisplay = new TextBoxDisplay(newText);
      else textBoxDisplay.setText(newText);
    }

    if (textBoxDisplay == null) {
      textBoxDisplay = new TextBoxDisplay(text);
    }
    textBoxDisplay.create();
  }

  public void hideDialogue() {
    if (textBoxDisplay != null) {
      textBoxDisplay.dispose();
    }
  }
}