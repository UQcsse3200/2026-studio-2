package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.services.ServiceLocator;

/** Shared layout and actor lifecycle for a screen's top-right action button. */
public abstract class TopRightButtonDisplay extends UIComponent {
  private final float buttonWidth;
  private final float buttonHeight;
  private final float padTop;
  private final float padRight;
  private final String upTexturePath;
  private final String downTexturePath;
  private Table table;

  protected TopRightButtonDisplay(
      float buttonWidth,
      float buttonHeight,
      float padTop,
      float padRight,
      String upTexturePath,
      String downTexturePath) {
    this.buttonWidth = buttonWidth;
    this.buttonHeight = buttonHeight;
    this.padTop = padTop;
    this.padRight = padRight;
    this.upTexturePath = upTexturePath;
    this.downTexturePath = downTexturePath;
  }

  @Override
  public void create() {
    super.create();
    createButton();
  }

  /** Subclasses can choose alternative art when the actors are created. */
  protected void createButton() {
    addButton(upTexturePath, downTexturePath);
  }

  /** Handles this screen's action when the button is pressed. */
  protected abstract void onButtonClicked();

  /** Adds the screen's textures using its configured button dimensions. */
  protected void addButton(String upPath, String downPath) {
    Texture up = ServiceLocator.getResourceService().getAsset(upPath, Texture.class);
    Texture down = ServiceLocator.getResourceService().getAsset(downPath, Texture.class);
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
            onButtonClicked();
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
