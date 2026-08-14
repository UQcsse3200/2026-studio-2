package com.csse3200.game.ui.dialogue;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;

public class TextBox extends Component {

  private static final float DEFAULT_LIFETIME = 3f;
  private static final float DEFAULT_X = Gdx.graphics.getWidth() / 2f - 100f; //centre
  private static final float DEFAULT_Y = 20f;

  private final float lifetime;
  private final float xPos;
  private final float yPos;

  private TextBoxDisplay textBoxDisplay;
  private final Text text;

  public TextBox(Text text) {
    this.lifetime = DEFAULT_LIFETIME;
    this.xPos = DEFAULT_X;
    this.yPos = DEFAULT_Y;
    this.text = text;
    this.textBoxDisplay = new TextBoxDisplay(text, this.lifetime, this.xPos, this.yPos);
  }

  public TextBox(Text text, float lifetime, float xPos, float yPos) {
    this.lifetime = lifetime;
    this.xPos = xPos;
    this.yPos = yPos;
    this.text = text;
    this.textBoxDisplay = new TextBoxDisplay(text, this.lifetime, this.xPos, this.yPos);
  }

  @Override
  public void create() {
    super.create();
    if (textBoxDisplay == null) {
      textBoxDisplay = new TextBoxDisplay(text, DEFAULT_LIFETIME, DEFAULT_X, DEFAULT_Y);
    }
    textBoxDisplay.create();
  }

  public void hideDialogue() {
    if (textBoxDisplay != null) {
      textBoxDisplay.dispose();
    }
  }
}