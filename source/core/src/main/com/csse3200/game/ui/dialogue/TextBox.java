package com.csse3200.game.ui.dialogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.components.Component;

public class TextBox extends Component {

  private final Text text;
  // default, redundant values
  private float lifetime = 3f;
  private float xPos = Gdx.graphics.getWidth() / 2f - 100f;
  private float yPos = 20f;
  private TextBoxDisplay textBoxDisplay;

  public TextBox(Text text, float lifetime, float xPos, float yPos) {
    this.lifetime = lifetime;
    this.xPos = xPos;
    this.yPos = yPos;
    this.text = text;
    this.textBoxDisplay = new TextBoxDisplay(text, this.lifetime, this.xPos, this.yPos, this);
    this.textBoxDisplay.setTextColor(Color.WHITE);
  }

  @Override
  public void create() {
    super.create();
    if (textBoxDisplay == null) {
      textBoxDisplay = new TextBoxDisplay(text, this.lifetime, this.xPos, this.yPos, this);
    }
    textBoxDisplay.create();
  }

  public void dispose() {
    super.dispose();
  }

  public void setTextColour(Color color) {
    textBoxDisplay.setTextColor(color);
  }
}
