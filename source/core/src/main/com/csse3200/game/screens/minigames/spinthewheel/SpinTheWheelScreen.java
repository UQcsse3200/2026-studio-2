package com.csse3200.game.screens.minigames.spinthewheel;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.minigames.spinthewheel.SpinTheWheelActions;
import com.csse3200.game.components.minigames.spinthewheel.SpinTheWheelDisplay;
import com.csse3200.game.components.minigames.spinthewheel.WheelConfig;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.screens.minigames.MinigameScreen;
import com.csse3200.game.services.ServiceLocator;

/** The screen for the spin the wheel minigame. */
public class SpinTheWheelScreen extends MinigameScreen {

  public SpinTheWheelScreen(GdxGame game) {
    super(game);
  }

  @Override
  protected String[] getTextures() {
    return WheelConfig.TEXTURES;
  }

  @Override
  protected Entity createUI() {
    Stage stage = ServiceLocator.getRenderService().getStage();
    return new Entity()
        .addComponent(new SpinTheWheelDisplay(WheelConfig.ITEMS))
        .addComponent(new InputDecorator(stage, 10))
        .addComponent(new SpinTheWheelActions(game));
  }
}
