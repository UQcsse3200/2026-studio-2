package com.csse3200.game.screens.minigames;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.minigames.MinigameSelectActions;
import com.csse3200.game.components.minigames.MinigameSelectDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.services.ServiceLocator;

/** The screen where the player picks which minigame to play. */
public class MinigameSelectScreen extends MinigameScreen {

  public MinigameSelectScreen(GdxGame game) {
    super(game);
  }

  @Override
  protected String[] getTextures() {
    return new String[] {
      "images/Buttons/back_up_btn.png",
      "images/Buttons/back_down_btn.png",
      "images/Buttons/spinTheWheel_up_btn.png",
      "images/Buttons/spinTheWheel_down_btn.png",
      "images/Buttons/blackJack_up_btn.png",
      "images/Buttons/blackJack_down_btn.png",
      "images/Buttons/cyclops_up_btn.png",
      "images/Buttons/cyclops_down_btn.png"
    };
  }

  @Override
  protected Entity createUI() {
    Stage stage = ServiceLocator.getRenderService().getStage();
    return new Entity()
        .addComponent(new MinigameSelectDisplay())
        .addComponent(new InputDecorator(stage, 10))
        .addComponent(new MinigameSelectActions(game));
  }
}
