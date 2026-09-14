package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.ui.UIComponent;

/**
 * A ui component for displaying the debug terminal. The terminal is positioned at the bottom of the
 * screen.
 */
public class TerminalDisplay extends UIComponent {
  private static final float Z_INDEX = 10f;
  private Terminal terminal;
  private Label label;

  @Override
  public void create() {
    super.create();
    addActors();
    terminal = entity.getComponent(Terminal.class);
    applyStyle();
  }

  private void addActors() {
    String message = "";
    label = new Label("> " + message, skin);
    label.setPosition(5f, 0);
    stage.addActor(label);
  }

  @Override
  public void draw(SpriteBatch batch) {
    if (terminal.isOpen()) {
      label.setVisible(true);
      String message = terminal.getEnteredMessage();
      label.setText("> " + message);
    } else {
      label.setVisible(false);
    }
  }
    private void applyStyle() {
        // Clone the style so we don't mutate a shared skin-wide style instance
        Label.LabelStyle style = new Label.LabelStyle(label.getStyle());
        style.fontColor = Color.WHITE;
        label.setStyle(style);
    }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  @Override
  public void dispose() {
    super.dispose();
    label.remove();
  }
}
