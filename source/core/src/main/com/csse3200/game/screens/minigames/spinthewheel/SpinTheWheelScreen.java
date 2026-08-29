package com.csse3200.game.screens.minigames.spinthewheel;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.minigames.spinthewheel.SpinTheWheelActions;
import com.csse3200.game.components.minigames.spinthewheel.SpinTheWheelDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.screens.minigames.MinigameScreen;
import com.csse3200.game.services.ServiceLocator;
import java.util.LinkedHashMap;
import java.util.Map;

/** The screen for the spin the wheel minigame. */
public class SpinTheWheelScreen extends MinigameScreen {
  private static final Map<String, Integer> items = createItems();

  public SpinTheWheelScreen(GdxGame game) {
    super(game);
  }

  /**
   * The items on the wheel, in the order they appear on it.
   *
   * @return the item labels mapped to their value
   */
  private static Map<String, Integer> createItems() {
    Map<String, Integer> wheelItems = new LinkedHashMap<>();
    wheelItems.put("Wood", 10);
    wheelItems.put("Stone", 20);
    wheelItems.put("Iron", 30);
    wheelItems.put("Gold", 50);
    return wheelItems;
  }

  @Override
  protected String[] getTextures() {
    return new String[0];
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
