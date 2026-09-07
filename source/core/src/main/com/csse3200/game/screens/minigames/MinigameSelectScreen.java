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
    // The menu is built entirely from the skin, so there is nothing extra to load.
    return new String[0];
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
