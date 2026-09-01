package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.ui.dialogue.Text;
import com.csse3200.game.ui.dialogue.TextBoxFactory;
import java.util.ArrayList;

public class TextBoxCommand implements Command {
  private final Text message;

  private static final float DEFAULT_LIFETIME = 3f;
  private static final float DEFAULT_X = Gdx.graphics.getWidth() / 2f - 100f; // centre
  private static final float DEFAULT_Y = 20f;

  private final float lifetime;
  private final float xPos;
  private final float yPos;

  public TextBoxCommand(String message) {
    this.message = new Text(message);
    this.lifetime = DEFAULT_LIFETIME;
    this.xPos = DEFAULT_X;
    this.yPos = DEFAULT_Y;
  }

  public TextBoxCommand(String message, float lifeTime, float x, float y) {
    this.message = new Text(message);
    this.lifetime = lifeTime;
    this.xPos = x;
    this.yPos = y;
  }

  @Override
  public boolean action(ArrayList<String> args) {
    System.out.println(message.getContent());
    TextBoxFactory textBoxFactory = new TextBoxFactory(message, lifetime, xPos, yPos);
    textBoxFactory.create();
    return true;
  }
}
