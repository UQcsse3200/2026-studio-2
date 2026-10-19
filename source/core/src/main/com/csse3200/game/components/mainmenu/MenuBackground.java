package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.services.ServiceLocator;

/** Builds the shared animated backdrop for the main menu and save selection screen. */
final class MenuBackground {
  private static final float OVERSCAN = 1.03f;
  private static final float SHAKE_AMOUNT = 12f;
  private static final float SHAKE_DURATION = 1f;

  private MenuBackground() {}

  /**
   * Creates a centered, oversized background with a repeating shake. The caller owns the actor; its
   * texture remains owned by the resource service.
   */
  static Image create(int screenWidth, int screenHeight) {
    Image background =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/backgrounds/main_menu_bg.png", Texture.class));
    float bgWidth = screenWidth * OVERSCAN;
    float bgHeight = screenHeight * OVERSCAN;
    background.setSize(bgWidth, bgHeight);
    background.setPosition(-(bgWidth - screenWidth) / 2f, -(bgHeight - screenHeight) / 2f);
    background.addAction(
        Actions.forever(
            Actions.sequence(
                Actions.moveBy(SHAKE_AMOUNT, 0f, SHAKE_DURATION, Interpolation.sine),
                Actions.moveBy(-SHAKE_AMOUNT, SHAKE_AMOUNT, SHAKE_DURATION, Interpolation.sine),
                Actions.moveBy(-SHAKE_AMOUNT, -SHAKE_AMOUNT, SHAKE_DURATION, Interpolation.sine),
                Actions.moveBy(SHAKE_AMOUNT, 0f, SHAKE_DURATION, Interpolation.sine))));
    return background;
  }
}