package com.csse3200.game.ui.dialogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.components.Component;

public class TextBoxFactory extends Component {

  private final Text text;
  // default, redundant values
  private float lifetime = 3f;
  private float xPos = Gdx.graphics.getWidth() / 2f - 100f;
  private float yPos = 20f;
  private TextBoxComponent textBoxComponent;

  public TextBoxFactory(Text text, float lifetime, float xPos, float yPos) {
    this.lifetime = lifetime;
    this.xPos = xPos;
    this.yPos = yPos;
    this.text = text;
    this.textBoxComponent = new TextBoxComponent(text, this.lifetime, this.xPos, this.yPos);
    this.textBoxComponent.setTextColor(Color.WHITE);
  }

  @Override
  public void create() {
    super.create();
    if (textBoxComponent == null) {
      textBoxComponent = new TextBoxComponent(text, this.lifetime, this.xPos, this.yPos);
    }
    textBoxComponent.create();
  }

  public void hideDialogue() {
    if (textBoxComponent != null) {
      textBoxComponent.dispose();
    }
  }
}
