package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/** Shared layout and actor lifecycle for a screen's top-right action button. */
public abstract class TopRightButtonDisplay extends UIComponent {
  private final float buttonWidth;
  private final float buttonHeight;
  private final float padTop;
  private final float padRight;
  private Table table;

  protected TopRightButtonDisplay(
      float buttonWidth, float buttonHeight, float padTop, float padRight) {
    this.buttonWidth = buttonWidth;
    this.buttonHeight = buttonHeight;
    this.padTop = padTop;
    this.padRight = padRight;
  }

  /** Adds the screen's textures and action using its configured button dimensions. */
  protected void addButton(Texture up, Texture down, Runnable onClick) {
    table = new Table();
    table.top().right();
    table.setFillParent(true);

    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    style.up = new TextureRegionDrawable(up);
    style.down = new TextureRegionDrawable(down);
    ImageButton button = new ImageButton(style);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            onClick.run();
          }
        });
    table.add(button).width(buttonWidth).height(buttonHeight).padTop(padTop).padRight(padRight);
    stage.addActor(table);
  }

  @Override
  public void draw(SpriteBatch batch) {
    // Draw is handled by the stage.
  }

  @Override
  public float getZIndex() {
    return 2f;
  }

  public void setVisible(boolean visible) {
    if (table != null) {
      table.setVisible(visible);
    }
  }

  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}
