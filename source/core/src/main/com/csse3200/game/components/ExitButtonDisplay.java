package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows an exit button in the top right corner of the screen. What pressing it does, and how big
 * the button is, is up to the subclass.
 */
public abstract class ExitButtonDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(ExitButtonDisplay.class);
  private static final float Z_INDEX = 2f;

  private final float buttonWidth;
  private final float buttonHeight;
  private final float padTop;
  private final float padRight;
  private Table table;

  /**
   * @param buttonWidth width of the button
   * @param buttonHeight height of the button
   * @param padTop space between the top of the screen and the button
   * @param padRight space between the right of the screen and the button
   */
  protected ExitButtonDisplay(float buttonWidth, float buttonHeight, float padTop, float padRight) {
    this.buttonWidth = buttonWidth;
    this.buttonHeight = buttonHeight;
    this.padTop = padTop;
    this.padRight = padRight;
  }

  /** Called when the exit button is pressed. */
  protected abstract void onExitClicked();

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    table = new Table();
    table.top().right();
    table.setFillParent(true);

    Texture exitUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/exit_up_btn.png", Texture.class);
    Texture exitDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/exit_down_btn.png", Texture.class);

    ImageButton.ImageButtonStyle exitButtonStyle = new ImageButton.ImageButtonStyle();
    exitButtonStyle.up = new TextureRegionDrawable(exitUpTexture);
    exitButtonStyle.down = new TextureRegionDrawable(exitDownTexture);

    ImageButton exitBtn = new ImageButton(exitButtonStyle);

    exitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Exit button clicked");
            onExitClicked();
          }
        });

    table.add(exitBtn).width(buttonWidth).height(buttonHeight).padTop(padTop).padRight(padRight);

    stage.addActor(table);
  }

  @Override
  public void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
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
