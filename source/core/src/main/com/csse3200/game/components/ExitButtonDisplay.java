package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.TopRightButtonDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows an exit button in the top right corner of the screen. What pressing it does, and how big
 * the button is, is up to the subclass.
 */
public abstract class ExitButtonDisplay extends TopRightButtonDisplay {
  private static final Logger logger = LoggerFactory.getLogger(ExitButtonDisplay.class);

  /**
   * @param buttonWidth width of the button
   * @param buttonHeight height of the button
   * @param padTop space between the top of the screen and the button
   * @param padRight space between the right of the screen and the button
   */
  protected ExitButtonDisplay(float buttonWidth, float buttonHeight, float padTop, float padRight) {
    super(buttonWidth, buttonHeight, padTop, padRight);
  }

  /** Called when the exit button is pressed. */
  protected abstract void onExitClicked();

  @Override
  public void create() {
    super.create();

    Texture exitUpTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/exit_up_btn.png", Texture.class);
    Texture exitDownTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/Buttons/exit_down_btn.png", Texture.class);

    addButton(
        exitUpTexture,
        exitDownTexture,
        () -> {
          logger.debug("Exit button clicked");
          onExitClicked();
        });
  }
}
