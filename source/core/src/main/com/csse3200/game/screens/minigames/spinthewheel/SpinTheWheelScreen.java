package com.csse3200.game.screens.minigames.spinthewheel;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.minigames.spinthewheel.SpinTheWheelActions;
import com.csse3200.game.components.minigames.spinthewheel.SpinTheWheelDisplay;
import com.csse3200.game.components.minigames.spinthewheel.WheelItem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.screens.minigames.MinigameScreen;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;

/** The screen for the spin the wheel minigame. */
public class SpinTheWheelScreen extends MinigameScreen {
  private static final List<WheelItem> items = createItems();
  private static final String[] TEXTURES = {
    "images/minigames/spinthewheel/wheel-disc.png",
    "images/minigames/spinthewheel/wheel-spoke.png",
    "images/minigames/spinthewheel/wheel-pointer.png",
  };

  public SpinTheWheelScreen(GdxGame game) {
    super(game);
  }

  /**
   * The items on the wheel, in the order they appear on it.
   *
   * @return the item labels mapped to their value
   */
  private static List<WheelItem> createItems() {
    return List.of(
        new WheelItem("Wood", 10),
        new WheelItem("Stone", 20),
        new WheelItem("Iron", 30),
        new WheelItem("Gold", 50));
  }

  @Override
  protected String[] getTextures() {
    return TEXTURES;
  }

  @Override
  protected Entity createUI() {
    Stage stage = ServiceLocator.getRenderService().getStage();
    return new Entity()
        .addComponent(new SpinTheWheelDisplay(items))
        .addComponent(new InputDecorator(stage, 10))
        .addComponent(new SpinTheWheelActions(game));
  }
}
